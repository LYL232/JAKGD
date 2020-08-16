<template>
  <div>
    <edit-relationship-dialog
            ref="editRelationshipDialog" @edited="updateRelationships"/>
    <div v-if="node">
      <create-relationship-dialog :node="node" ref="createRelationshipDialog"
                                  @created="updateRelationships"/>
      <add-node-card-dialog ref="addCardDialog" :node="node"/>
      <edit-node-dialog ref="editNodeDialog" :node="node"
                        @edited="fetchAndUpdateBaseInfo"/>
      <div style="padding: 10px 20px">
        <div style="margin-bottom: 10px">
          <div style="align-items: center; height: 50px">
            <popover-node-director
                    v-for="(item, i) in references" :key="'popover-ref' + i"
                    @relationship-deleted="updateRelationships"
                    @edit-relationship="editRelationship"
                    :node="item.node" :relationship="item.relationship"/>
            <i v-if="references.length > 0" class="el-icon-d-arrow-right"/>
            <h1 v-if="node.properties.name" style="display: inline">({{node.id}}):{{node.properties.name}}</h1>
            <h1 v-if="!node.properties.name" style="display: inline">{{node.id}}:无名</h1>

            <i v-if="applications.length > 0" class="el-icon-d-arrow-right"/>

            <popover-node-director
                    v-for="(item, i) in applications"
                    @relationship-deleted="relationshipDeleted(applications, i)"
                    @edit-relationship="editRelationship"
                    :key="'popover-app' + i" :node="item.node"
                    :relationship="item.relationship"/>

            <h3 style="display: inline; float: right">作者:{{node.properties.author}}</h3>
          </div>
          <div>
            <el-button-group style="float:right">
              <el-button type="primary" icon="el-icon-refresh-right" :loading="buttonLoading"
                         @click="updateAll"/>
              <el-button type="primary" icon="el-icon-edit" :loading="buttonLoading"
                         @click="$refs.editNodeDialog.show()"/>
              <el-button type="primary" icon="el-icon-share" :loading="buttonLoading"
                         @click="clickExpandButton"/>
              <el-button type="primary" icon="el-icon-document-add" :loading="buttonLoading"
                         @click="$refs.addCardDialog.show()"/>
              <el-button type="primary" icon="el-icon-connection" :loading="buttonLoading"
                         @click="$refs.createRelationshipDialog.show()"/>
              <el-button type="danger" icon="el-icon-delete" @click="clickDeleteButton" :loading="buttonLoading"/>
            </el-button-group>
          </div>
        </div>
        <div v-if="node.labels">
          <i class="el-icon-collection-tag" style="margin-right: 10px"/>
          <el-tag :key="node.properties.name + '-label-' + tag"
                  v-for="(tag, i) in node.labels"
                  :type="colors[i % colors.length]"
                  size="medium"
                  style="font-size: 20px">
            {{globalData.nodeLabelNameMap(tag)}}
          </el-tag>
          <el-tag v-if="node.labels.length === 0" :type="colors[0]"
                  size="medium" style="font-size: 20px">未定义
          </el-tag>
        </div>
        <p style="margin-top: 10px" v-if="node.properties.summary">
          摘要:{{node.properties.summary}}
        </p>
      </div>
      <el-divider/>
      <properties-box :properties="displayProperties"/>
      <el-divider v-if="displayProperties.length > 0"/>
      <relationship-block
              v-for="item in relationships"
              :key="'relationship-block-' + item.type"
              @edit-relationship="editRelationship"
              @relationship-deleted="updateRelationships"
              :as-start-node="item.asStartNode"
              :as-end-node="item.asEndNode"
              :node="node"
              :type="item.type"/>
      <node-card-list v-if="node" ref="nodeCardList" :node="node"/>
    </div>
    <h2 v-if="!node&& fetchedInfo">没有数据</h2>
  </div>
</template>

<script>
import '../../../../static/mathjax/tex-chtml'
import NodeCardList from './NodeCardList'
import PopoverNodeDirector from './PopoverNodeDirector'
import AddNodeCardDialog from './AddNodeCardDialog'
import EditRelationshipDialog from './EditRelationshipDialog'
import EditNodeDialog from './EditNodeDialog'
import RelationshipBlock from './RelationshipBlock'
import PropertiesBox from './PropertiesBox'
import CreateRelationshipDialog from './CreateRelationshipDialog'

export default {
  name: 'NodeView',
  data() {
    return {
      references: [],
      applications: [],
      relationships: [],
      colors: ['success', 'primary', 'info', 'danger', 'warning'],
      node: null,
      buttonLoading: false,
      fetchedInfo: false,
      notDisplayProperties: new Set(['summary', 'name', 'author']),
      nodeMap: null,
      relationshipMap: null,
    }
  },
  computed: {
    /**
     * 展示在页面上的属性
     * @returns [{name, value}]
     */
    displayProperties() {
      let res = []
      for (let name in this.node.properties) {
        if (Object.prototype.hasOwnProperty.call(this.node.properties, name)) {
          if (this.notDisplayProperties.has(name)) {
            continue
          }
          res.push({
            name: this.globalData.propertyNameMap(name),
            value: this.node.properties[name],
          })
        }
      }
      return res
    },
  },
  props: {
    nodeId: Number,
  },
  mounted() {
    this.fetchAndUpdateBaseInfo()
    this.updateRelationships()
  },
  components: {
    CreateRelationshipDialog,
    PropertiesBox,
    RelationshipBlock,
    EditNodeDialog,
    EditRelationshipDialog,
    AddNodeCardDialog,
    NodeCardList,
    PopoverNodeDirector,
  },
  methods: {
    updateAll() {
      this.fetchAndUpdateBaseInfo()
      this.updateRelationships()
      this.updateCards()
    },
    /**
     * 获取节点的基本信息并更新本地信息
     */
    fetchAndUpdateBaseInfo() {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.1)',
      })
      this.axios.get('/api/node/' + this.nodeId).then(response => {
        this.node = response.data
        loading.close()
        this.fetchedInfo = true
      }).catch(err => {
        this.util.errorHint(err, '获取信息失败')
        loading.close()
        this.node = null
        this.fetchedInfo = true
      })
    },
    updateRelationships() {
      this.axios.get('/api/node/' + this.nodeId + '/neighborhood/direct?limit=1000').then(response => {
        let data = response.data
        this.nodeMap = new Map()
        this.relationshipMap = new Map()
        data.nodes.forEach(node => {
          this.nodeMap.set(node.id, node)
        })

        let relationshipsByType = new Map(),
          that = this

        this.references = []
        this.applications = []

        data.relationships.forEach(relationship => {
          if (!this.relationshipMap.has(relationship.id)) {
            this.relationshipMap.set(relationship.id, relationship)
          }
          let relType = relationship.type
          if (relType === '__refer') {
            if (this.nodeId === relationship.startNode) {
              that.references.push({
                node: this.nodeMap.get(relationship.endNode),
                relationship,
              })
            } else {
              that.applications.push({
                node: this.nodeMap.get(relationship.startNode),
                relationship,
              })
            }
            return
          }
          relType = this.globalData.relationshipTypeNameMap(relationship.type)
          if (!relationshipsByType.has(relType)) {
            relationshipsByType.set(relType, {asEndNode: [], asStartNode: []})
          }
          if (this.nodeId === relationship.startNode) {
            relationshipsByType.get(relType).asStartNode.push({
              relationship,
              endNode: this.nodeMap.get(relationship.endNode),
            })
          } else {
            relationshipsByType.get(relType).asEndNode.push({
              relationship,
              startNode: this.nodeMap.get(relationship.startNode),
            })
          }
        })
        this.relationships = []
        relationshipsByType.forEach((value, key) => {
          that.relationships.push({
            type: key,
            asStartNode: value.asStartNode,
            asEndNode: value.asEndNode,
          })
        })

      }).catch(err => {
        if (err.errorMsg) {
          this.$message.error('获取关系数据错误' + err.errorMsg)
        } else {
          this.$message.error('获取关系数据错误')
        }
      })
    },
    updateCards() {
      this.$refs.nodeCardList.fetchCardsData()
    },

    clickDeleteButton() {
      if (!this.$refs.nodeCardList.noCards()) {
        this.$message.info('要删除此节点, 需要先删除其所有的卡片')
        return
      }
      this.$confirm('永久删除该节点?', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        this.axios.delete('/api/node/' + this.node.id).then(() => {
          this.$message({
            type: 'success',
            message: '删除成功!',
          })
          this.bus.$emit('removeTab', {
            type: 'node-view',
            node: {id: this.nodeId},
          })
        }).catch(this.util.simpleErrorHandler('删除错误'))
      }).catch(() => {
      })
    },
    clickExpandButton() {
      if (!this.node) {
        this.$message('无有效数据')
        return
      }
      this.buttonLoading = true
      this.axios.get('/api/node/' + this.node.id + '/neighborhood').then(response => {
        this.buttonLoading = false
        let nodes = response.data.nodes, hasThisNode = false
        for (let node of nodes) {
          if (node.id === this.node.id) {
            hasThisNode = true
            break
          }
        }
        if (!hasThisNode) {
          nodes.push(this.node)
        }

        this.bus.$emit('addHomeCard', {
          cardType: 'graph',
          cardData: {
            graph: {
              nodes: response.data.nodes,
              relationships: response.data.relationships,

            },
          },
          cardTitle: this.util.getNodeTitle(this.node) + '的邻域',
        })
      }).catch(err => {
        this.buttonLoading = false
        if (err.errorMsg) {
          this.$message.error('获取领域数据失败: ' + err.errorMsg)
        } else {
          this.$message.error('获取领域数据失败: 未知错误')
        }
      })
    },
    /**
     * 关系被删除事件: 更新当前关系视图
     * @param array 需要更新的数组
     * @param index 需要删除的下标
     */
    relationshipDeleted(array, index) {
      array.splice(index, 1)
    },
    /**
     * 编辑关系
     * @param relationship 关系对象
     */
    editRelationship(relationship) {
      this.$refs.editRelationshipDialog.show(relationship)
    },
  },
}
</script>

<!--suppress CssUnusedSymbol -->
<style scoped>
  .el-tag + .el-tag {
    margin-left: 10px;
  }
</style>