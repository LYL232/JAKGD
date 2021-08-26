<template>
  <el-card class="card" :style="hasData() ? 'height:auto' : ''">
    <template #header>
      <div class="clear-fix" style="min-height: 40px;">
        <h3 v-if="cardTitle && cardTitle !== ''"
            style="text-align: center; display: inline">
          {{ cardTitle }}</h3>
        <el-button style="float:right" type="primary" size="medium" round
                   @click="clickCloseButton">
          关闭
        </el-button>
      </div>
    </template>
    <el-row>
      <el-col :span="18">
        <div :id="'graph-window-' + cardId" :style="hasData() ? 'height:' + windowHeight * 0.7 + 'px;' : ''"/>
      </el-col>
      <el-col :span="6">
        <div style="margin-left: 10px;">
          <h3 v-if="nodeSelected" style="display: inline">
            {{ util.getNodeTitle(nodeSelected) }}
          </h3>
          <h3 v-if="!nodeSelected && hasData()">()</h3>
          <el-button
              type="primary"
              @click="clearNodeSelected" size="mini" round
              style="float: right"
              v-if="nodeSelected">
            取消选择
          </el-button>
        </div>
        <el-button-group v-if="nodeSelected" style="margin: 10px; float: right;">
          <el-button type="primary" icon="el-icon-share"
                     @click="clickExpandButton" :loading="expandButtonLoading"/>
          <el-button type="primary" icon="el-icon-document" @click="clickDocumentButton"/>
          <el-button type="primary" icon="el-icon-circle-close" @click="clickRemoveButton"/>
          <el-button type="danger" icon="el-icon-delete" @click="clickDeleteButton" :loading="deleteButtonLoading"/>
        </el-button-group>

        <div :id="'graph-window-' + cardId + 'info'" style="display: inline"/>
      </el-col>
    </el-row>
    <span v-if="!hasData()">没有数据</span>
  </el-card>
</template>

<script>
import GraphWindow from '../../../class/GraphWindow'

import '../../../class/GraphWindow.css'

export default {
  data() {
    return {
      nodeSelected: null,
      // 图展示对象
      graphWindow: null,
      expandButtonLoading: false,
      deleteButtonLoading: false,
      windowHeight: document.documentElement.clientHeight,
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
      let that = this
      this.graphWindow = new GraphWindow('#graph-window-' + this.cardId, {
        onNodeClick: function (event, node) {
          that.nodeSelected = node
          that.graphWindow.setHighlightNodeSet(new Set().add(node.id))
        },
        onNodeDoubleClick: function (event, node) {
          that.addNodeViewTab(node)
        },
        infoPanelSelector: '#graph-window-' + this.cardId + 'info',
      })
      // 初始化时随机选择一个节点高亮并固定在屏幕的指定位置
      this.nodeSelected = this.graphData.nodes[
          this.util.randomNumInRange(0, this.graphData.nodes.length)]
      // TODO: 将被选择的节点放在图窗口的中心位置
      this.graphWindow.appendGraph(this.graphData,
          new Map().set(this.nodeSelected.id, {x: 400, y: 300}))
      this.graphWindow.stickNode(this.nodeSelected.id)
      this.graphWindow.setHighlightNodeSet(new Set().add(this.nodeSelected.id))
    }
  },
  methods: {

    /**
     * 判断是否有数据
     * @returns {boolean}
     */
    hasData() {
      if (this.graphWindow) {
        return this.graphWindow.nodeCount() > 0
      } else {
        return false
      }
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
            this.expandGraph(data, this.nodeSelected)
          }).catch(err => {
        this.util.errorHint(err, '获取节点领域错误')
        this.expandButtonLoading = false
      })
    },
    /**
     * 以source节点为中心追加图数据
     * @param graph Object: {
     *   nodes: []
     *   relationships: []
     * }
     * @param source node: 新的图数据在source node的周围产生
     */
    expandGraph(graph, source) {
      if (graph.nodes.length === 0 && graph.relationships.length === 0) {
        return
      }
      let nodePoints = new Map()
      for (let node of graph.nodes) {
        // 设置新节点(除源节点)的位置在源节点周围
        if (node.id !== source.id) {
          nodePoints.set(node.id, {
            x: source.x + Math.random(), y: source.y + Math.random(),
          })
        }
      }
      this.graphWindow.stickNode(this.nodeSelected.id)
      this.graphWindow.appendGraph(graph, nodePoints)
    },
    addNodeViewTab(node) {
      this.bus.$emit('newTab', {type: 'node-view', node: node})
    },
    clickRemoveButton() {
      this.graphWindow.removeGraph({
        nodeIdSet: new Set().add(
            this.nodeSelected.id,
        ),
      })
      this.clearNodeSelected()
    },
    clearNodeSelected() {
      this.graphWindow.clearHighlightNodeSet()
      this.nodeSelected = null
    },
    clickDeleteButton() {
      this.$confirm('确定删除此节点? 要求节点不能有关系且联系着的文档. ', '删除节点', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        this.deleteButtonLoading = true
        this.axios.delete('/api/node/' + this.nodeSelected.id).then(() => {
          this.$message({
            type: 'success',
            message: '删除成功',
          })
          this.graphWindow.removeGraph({
            nodeIdSet: new Set().add(
                this.nodeSelected.id,
            ),
          })
          this.deleteButtonLoading = false
          this.nodeSelected = null
        }).catch((err) => {
          this.util.errorHint(err, '删除节点失败')
          this.deleteButtonLoading = false
        })
      }).catch(() => {})
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