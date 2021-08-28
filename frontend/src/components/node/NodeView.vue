<template>
  <div class="node-view">
    <div v-if="node">
      <div class="align-center-flex">
        <popover-node-director
            v-for="(item, i) in references" :key="'node-view-' + node.id + 'popover-ref' + i"
            @relationship-deleted="updateRelationships"
            @edit-relationship="editRelationship"
            :node="item.node" :relationship="item.relationship"/>
        <right-icon v-if="references.length > 0" class="ref-app-rel"/>
        <h1 v-if="node.properties.name" style="display: inline">({{ node.properties.name }})</h1>
        <h1 v-if="!node.properties.name" style="display: inline">(无名)</h1>
        <right-icon v-if="applications.length > 0" class="ref-app-rel"/>
        <popover-node-director
            v-for="(item, i) in applications"
            @relationship-deleted="relationshipDeleted(applications, i)"
            @edit-relationship="editRelationship"
            :key="'node-view-' + node.id + 'popover-app' + i" :node="item.node"
            :relationship="item.relationship"/>
        <div style="margin-left: auto;">
          <i class="el-icon-s-help" style="font-size: 25px;">{{ node.id }}</i>
          <br/>
          <i class="el-icon-user-solid" style="font-size: 25px;">{{ node.properties.author }}</i>
        </div>
      </div>
      <el-divider/>
      <div class="align-center-flex">
        <el-col :span="16" class="overview">
          <tag-list :node="node" class="overview-item"/>
          <p v-if="node.properties.summary" class="overview-item">
            <i class="header-icon el-icon-s-order"/>
            {{ node.properties.summary }}
          </p>
          <el-collapse class="overview-item">
            <el-collapse-item>
              <template #title>
                <i class="el-icon-s-unfold" style="font-size: 20px;"/>
              </template>
              <properties-table :properties="displayProperties"/>
            </el-collapse-item>
          </el-collapse>
        </el-col>
        <el-col :span="8" style="align-self: start;">
          <div style="float:right;">
            <div class="overview-item">
              <el-button-group>
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
            <div class="overview-item" style="float: right;">
              <i class="el-icon-document-add date-with-icon">{{ node.properties.created }}</i>
              <i class="el-icon-edit-outline date-with-icon">{{ node.properties.updated }}</i>
            </div>
          </div>

        </el-col>
      </div>
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
    <edit-relationship-dialog
        ref="editRelationshipDialog" @edited="updateRelationships"/>
    <create-relationship-dialog :node="node" ref="createRelationshipDialog"
                                @created="updateRelationships"/>
    <add-node-card-dialog ref="addCardDialog" :node="node"/>
    <edit-node-dialog ref="editNodeDialog" :node="node"
                      @edited="fetchAndUpdateBaseInfo"/>
  </div>
</template>

<script>
import '../../../../static/mathjax/tex-chtml.js'
import {Right} from '@element-plus/icons'
import {defineAsyncComponent} from 'vue'

const NodeCardList = defineAsyncComponent(() => import('./NodeCardList.vue')),
    PopoverNodeDirector = defineAsyncComponent(() => import('./PopoverNodeDirector.vue')),
    AddNodeCardDialog = defineAsyncComponent(() => import('./AddNodeCardDialog.vue')),
    EditRelationshipDialog = defineAsyncComponent(() => import('./EditRelationshipDialog.vue')),
    EditNodeDialog = defineAsyncComponent(() => import('./EditNodeDialog.vue')),
    RelationshipBlock = defineAsyncComponent(() => import('./RelationshipBlock.vue')),
    PropertiesTable = defineAsyncComponent(() => import('./PropertiesTable.vue')),
    CreateRelationshipDialog = defineAsyncComponent(() => import('./CreateRelationshipDialog.vue')),
    TagList = defineAsyncComponent(() => import('./TagList.vue'))

// 识别latex语法的MathJax的配置
// noinspection JSUnusedGlobalSymbols
window.MathJax.config = {
  startup: {
    ready: () => {
      window.MathJax.startup.defaultReady()
      window.MathJax.startup.promise.then(() => {
      })
    },
  },
  tags: 'all', // 为方程式编号
  tagSide: 'left', // 方程式编号的位置
  tex: {
    processEscapes: true,
    processEnvironments: true, // process \begin{xxx}...\end{xxx} outside math mode
    processRefs: true, // process \ref{...} outside of math mode
    inlineMath: [
      ['$', '$'],
      ['\\(', '\\)'],
    ],
    displayMath: [ // start/end delimiter pairs for display math
      ['$$', '$$'],
      ['\\[', '\\]'],
    ],
  },
}

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
      let res = [{
        name: 'id',
        value: this.node.id,
      }]
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
    TagList,
    CreateRelationshipDialog,
    PropertiesTable,
    RelationshipBlock,
    EditNodeDialog,
    EditRelationshipDialog,
    AddNodeCardDialog,
    NodeCardList,
    PopoverNodeDirector,
    RightIcon: Right,
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
        this.util.errorHint(err, '获取关系数据错误')
      })
    },
    updateCards() {
      this.$refs.nodeCardList.fetchCardsData()
    },

    clickDeleteButton() {
      if (!this.$refs.nodeCardList.noCards()) {
        this.$notify.info({
          title: '操作错误',
          message: '要删除此节点, 需要先删除其所有的卡片',
        })
        return
      }
      this.$confirm('永久删除该节点?', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        this.axios.delete('/api/node/' + this.node.id).then(() => {
          this.$notify({
            title: '成功',
            type: 'success',
            message: '删除成功',
          })
          this.bus.$emit('removeTab', {
            type: 'node-view',
            node: {id: this.nodeId},
          })
        }).catch(err => {
          this.util.errorHint(err, '删除错误')
        })
      }).catch(() => {
      })
    },
    clickExpandButton() {
      if (!this.node) {
        this.$notify.error({
          title: '未知错误',
          message: '无有效数据',
        })
        return
      }
      this.buttonLoading = true
      this.axios.get('/api/node/' + this.node.id + '/neighborhood?limit=25').then(response => {
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
            nodes: response.data.nodes,
            relationships: response.data.relationships,
          },
          cardTitle: this.util.getNodeTitle(this.node) + '的邻域',
        })
      }).catch(err => {
        this.buttonLoading = false
        this.util.errorHint(err, '获取领域数据失败')
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

<style scoped>
.node-view {
  width: 70%;
  margin-left: auto;
  margin-right: auto;
}

.overview {
  margin: 0 5px 20px 5px;
  padding: 5px 5px 0 5px;
}

.overview-item + .overview-item {
  margin-top: 10px;
}

.align-center-flex {
  display: flex;
  align-items: center;
}

.date-with-icon {
  display: block;
  font-size: 20px;
}

.date-with-icon + .date-with-icon {
  margin-top: 10px;
}

.ref-app-rel {
  width: 2em;
  height: 2em;
}
</style>