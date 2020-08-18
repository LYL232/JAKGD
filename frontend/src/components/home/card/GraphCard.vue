<template>
  <el-card class="card" :style="neo4jData ? 'height:700px' : ''">
    <div slot="header" class="clear-fix">
      <el-row>
        <el-col :span="8">
          <el-button type="primary" icon="el-icon-error"
                     @click="nodeSelected = null" size="mini" circle
                     v-if="nodeSelected"></el-button>
          <h3 v-if="nodeSelected" style="display: inline">
            {{util.getNodeTitle(nodeSelected)}}
          </h3>
          <h3 v-if="!nodeSelected">()</h3>
        </el-col>
        <el-col :span="8" style="align-items: center">
          <h3 v-if="cardTitle && cardTitle !== ''" style="text-align: center">{{cardTitle}}</h3>
        </el-col>
        <el-col :span="8" style="float:right">
          <el-button style="float:right" type="warning"
                     icon="el-icon-error" circle size="small"
                     @click="clickCloseButton"></el-button>
          <el-button-group v-if="nodeSelected" style="float:right; margin-right: 10px;">
            <el-button type="primary" icon="el-icon-share"
                       @click="clickExpandButton" :loading="expandButtonLoading"></el-button>
            <el-button type="primary" icon="el-icon-document" @click="clickDocumentButton"></el-button>
            <el-button type="danger" icon="el-icon-delete"
                       @click="clickDeleteButton" :loading="deleteButtonLoading"></el-button>
          </el-button-group>
        </el-col>
      </el-row>
    </div>
    <div :id="'neo4jd3-' + cardId" :style="neo4jData ? 'height:550px' : ''"></div>
    <span v-if="neo4jData === null">没有数据</span>
  </el-card>
</template>

<script>
const Neo4jd3 = require('../../../../../static/neo4jd3/neo4jd3-adjusted')
import '../../../../../static/neo4jd3/neo4jd3.min.css'

export default {
  data() {
    return {
      neo4jData: null,
      nodesMap: null,
      relationshipsMap: null,
      nodeSelected: null,
      // 图展示对象
      neo4jD3: null,
      expandButtonLoading: false,
      deleteButtonLoading: false,
    }
  },
  props: {
    cardId: Number,
    // 初始图数据, 不随数据改变而改变
    graphData: {
      type: Object,
      default: null,
    },
    cardTitle: {
      type: String,
      default: '',
    },
    nodeRadius: {
      type: Number,
      default: 30,
    },
    minCollision: {
      type: Number,
      default: 120,
    },
    // highlight: [
    //   {
    //     class: 'Project',
    //     property: 'name',
    //     value: 'neo4jd3',
    //   }, {
    //     class: 'User',
    //     property: 'userId',
    //     value: 'eisman',
    //   },
    // ],
    highlight: {
      type: Object,
      default: null,
    },
    // icons: {
    //   'Api': 'gear',
    //   'Cookie': 'paw',
    //   'Email': 'at',
    //   'Git': 'git',
    //   'Github': 'github',
    //   'Google': 'google',
    //   'Ip': 'map-marker',
    //   'Issues': 'exclamation-circle',
    //   'Language': 'language',
    //   'Options': 'sliders',
    //   'Password': 'lock',
    //   'Phone': 'phone',
    //   'Project': 'folder-open',
    //   'SecurityChallengeAnswer': 'commenting',
    //   'User': 'user',
    //   'zoomFit': 'arrows-alt',
    //   'zoomIn': 'search-plus',
    //   'zoomOut': 'search-minus'
    // },
    icons: {
      type: Object,
      default: null,
    },
    // images: {
    //   'Address': 'img/twemoji/1f3e0.svg',
    //   'BirthDate': 'img/twemoji/1f382.svg',
    //   'Cookie': 'img/twemoji/1f36a.svg',
    //   'CreditCard': 'img/twemoji/1f4b3.svg',
    //   'Device': 'img/twemoji/1f4bb.svg',
    //   'Email': 'img/twemoji/2709.svg',
    //   'Git': 'img/twemoji/1f5c3.svg',
    //   'Github': 'img/twemoji/1f5c4.svg',
    //   'icons': 'img/twemoji/1f38f.svg',
    //   'Ip': 'img/twemoji/1f4cd.svg',
    //   'Issues': 'img/twemoji/1f4a9.svg',
    //   'Language': 'img/twemoji/1f1f1-1f1f7.svg',
    //   'Options': 'img/twemoji/2699.svg',
    //   'Password': 'img/twemoji/1f511.svg',
    //   'Project': 'img/twemoji/2198.svg',
    //   'Project|name|neo4jd3': 'img/twemoji/2196.svg',
    //   'User': 'img/twemoji/1f600.svg'
    // },
    images: {
      type: Object,
      default: null,
    },
  },
  name: 'GraphCard',
  mounted() {
    if (this.graphData && this.graphData.nodes
      && this.graphData.nodes.length > 0) {
      // 初始化加载图数据
      this.setNeo4jData(this.graphData)
      let that = this
      this.neo4jD3 = new Neo4jd3('#neo4jd3-' + this.cardId, {
        highlight: this.highlight,
        icons: this.icons,
        images: this.images,
        minCollision: this.minCollision,
        neo4jData: this.neo4jData,
        nodeRadius: this.nodeRadius,
        onNodeClick: function(node) {
          that.nodeSelected = node
        },
        onNodeDoubleClick: function(node) {
          that.addNodeViewTab(node)
        },
        onRelationshipDoubleClick: function(relationship) {
          console.log('double click on relationship: ' + JSON.stringify(relationship))
        },
        zoomFit: true,
      })
    }
  },
  methods: {
    /**
     * 将图数据转换并存到this.neo4jData上, 方便neo4jd3直接读取
     * @param data
     */
    setNeo4jData(data) {
      let nodes = data.nodes, relationships = data.relationships
      this.nodesMap = new Map()
      this.relationshipsMap = new Map()
      this.transformNodes(nodes)
      this.transformRelationships(relationships)
      this.neo4jData = {results: [{data: [{graph: {nodes, relationships}}]}]}
    },

    /**
     * 转化节点数据为易于展示的形式
     * @param nodes 节点对象数组
     */
    transformNodes(nodes) {
      for (let i = 0; i < nodes.length; ++i) {
        let node = nodes[i]
        // 删除不必要属性 属性名映射
        for (let name in node.properties) {
          if (Object.prototype.hasOwnProperty.call(node.properties, name)) {
            if (this.globalData.internalProperties.has(name)) {
              delete node.properties[name]
            } else {
              let mapName = this.globalData.propertyNameMap(name)
              if (mapName !== name) {
                node.properties[mapName] = node.properties[name]
                delete node.properties[name]
              }
            }
          }
        }
        this.nodesMap.set(node.id, node)
        for (let j = 0; j < node.labels.length; ++j) {
          node.labels[j] = this.globalData.nodeLabelNameMap(node.labels[j])
        }
        if (node.labels.length > 1) {
          let pos = this.util.randomNumInRangeWithA(0, node.labels.length),
            posLabel = node.labels[pos]
          node.labels[pos] = node.labels[0]
          node.labels[0] = posLabel
        }
      }
    },

    /**
     * 转换关系对象为易于展示的形式
     * @param relationships 关系对象数组
     */
    transformRelationships(relationships) {
      for (let i = 0; i < relationships.length; ++i) {
        let relationship = relationships[i]
        for (let name in relationship.properties) {
          if (Object.prototype.hasOwnProperty.call(relationship.properties, name)) {
            if (this.globalData.internalProperties.has(name)) {
              delete relationship.properties[name]
            } else {
              let mapName = this.globalData.propertyNameMap(name)
              if (mapName !== name) {
                relationship.properties[mapName] = relationship.properties[name]
                delete relationship.properties[name]
              }
            }
          }
        }
        relationships[i].type = this.globalData.relationshipTypeNameMap(relationship.type)
        this.relationshipsMap.set(relationships.id, relationships)
      }
    },

    /**
     * 删除本地数据中的指定id的node和相关的关系并更新图
     * TODO: d3删除更新出问题
     * @param id
     */
    deleteNode(id) {
      if (!this.neo4jData || !this.neo4jD3) {
        return
      }
      let nodes = this.neo4jData.results[0].data[0].graph.nodes,
        relationships = this.neo4jData.results[0].data[0].graph.nodes
      for (let i = 0; i < nodes.length; ++i) {
        if (nodes[i].id === id) {
          nodes.splice(i, 1)
        }
      }
      for (let i = 0; i < relationships.length; ++i) {
        if (relationships[i].startNode === id || relationships[i].endNode === id) {
          relationships.splice(i, 1)
        }
      }
      // this.neo4jD3.updateWithNeo4jData(this.neo4jData)
    },
    clickCloseButton() {
      this.$emit('card-close', this.cardId)
    },
    clickDocumentButton() {
      if (this.nodeSelected) {
        this.addNodeViewTab(this.nodeSelected)
      }
    },
    clickExpandButton() {
      this.expandButtonLoading = true
      this.axios.get('/api/node/' + this.nodeSelected.id + '/neighborhood/direct').then(
        response => {
          let data = response.data
          this.expandButtonLoading = false
          this.appendGraph(data, this.nodeSelected)
        }).catch(err => {
        this.util.errorHint(err, '获取节点领域错误')
        this.expandButtonLoading = false
      })
    },
    /**
     * 添加图数据: 自动根据nodesMap 和relationshipsMap去重
     * @param graph Object: {
     *   nodes: []
     *   relationships: []
     * }
     * @param source node: 新的图数据在source node的周围产生
     */
    appendGraph(graph, source) {
      let nodes = [], relationships = []
      for (let node of graph.nodes) {
        if (!this.nodesMap.has(node.id)) {
          node.x = source.x ? source.x : 0
          node.y = source.y ? source.y : 0
          for (let j = 0; j < node.labels.length; ++j) {
            node.labels[j] = this.globalData.nodeLabelNameMap(node.labels[j])
          }
          nodes.push(node)
          this.nodesMap.set(node.id, node)
        }
      }
      for (let relationship of graph.relationships) {
        if (!this.relationshipsMap.has(relationship.id)) {
          relationship.type = this.globalData.relationshipTypeNameMap(relationship.type)
          relationship.source = relationship.startNode
          relationship.target = relationship.endNode
          relationships.push(relationship)
          this.relationshipsMap.set(relationship.id, relationship)
        }
      }
      // let data = this.neo4jD3.randomD3Data(this.nodeSelected, 2)
      if (nodes.length === 0 && relationships.length === 0) {
        return
      }
      this.transformNodes(nodes)
      this.transformRelationships(relationships)
      this.neo4jD3.updateWithD3Data({nodes, relationships})
    },
    addNodeViewTab(node) {
      this.bus.$emit('newTab', {type: 'node-view', node: node})
    },
    clickDeleteButton() {
      this.$confirm('永久删除该节点, 需要先删除其所有的关系和附属文档, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        // this.deleteNode(this.nodeSelected.id)
        this.deleteButtonLoading = true
        this.axios.delete('/api/node/' + this.nodeSelected.id).then(() => {
          this.$notify({
            title: '成功',
            type: 'success',
            message: '删除成功',
          })
          this.deleteButtonLoading = false
        }).catch(err => {
          this.util.errorHint(err, '删除失败')
          this.deleteButtonLoading = false
        })
      }).catch(() => {
      })
    },
  },
}
</script>

<style scoped>

  .card {
    width: 100%;
    margin-right: auto;
    margin-left: auto;
    margin-top: 10px;
    bottom: 10px;
    padding: 10px;
  }

  .clear-fix:before,
  .clear-fix:after {
    display: table;
    content: "";
  }

</style>