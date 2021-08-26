<template>
  <el-card class="card">
    <template #header class="clear-fix">
      <el-row>
        <el-col :span="8" style="align-items: center">
          <h3 v-if="cardTitle && cardTitle !== ''" style="text-align: center">{{ cardTitle }}</h3>
        </el-col>
        <el-col :span="16" style="float:right">
          <el-button style="float:right" type="primary" size="medium" round
                     @click="clickCloseButton">
            关闭
          </el-button>
        </el-col>
      </el-row>
    </template>
    <h3 v-if="hasNodeData()">节点</h3>
    <div v-if="hasNodeData()" class="table">
      <el-table :data="graphData.nodes" class="table">
        <el-table-column width="auto" property="id" label="id"/>
        <el-table-column width="auto" property="properties.name" label="名字"/>
        <el-table-column width="auto" property="properties.summary" label="摘要"/>
        <el-table-column width="auto" label="标签">
          <template #default="scope">
            <el-tag :key="'graph-data-table-' + cardId + '-' + scope.row.id + tag"
                    v-for="tag in scope.row.labels">
              {{ mapTagName(tag) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column width="auto" label="属性">
          <template #default="scope">
            <popover-node-director :node="scope.row"/>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-divider v-if="hasRelationshipData() && hasNodeData()"></el-divider>
    <h3 v-if="hasRelationshipData()">关系</h3>
    <div v-if="hasRelationshipData()" class="table">
      <el-table :data="graphData.relationships">
        <el-table-column width="auto" property="id" label="id"/>
        <el-table-column width="auto" label="起始节点">
          <template #default="scope">
            <popover-node-director :node="nodeMap.get(scope.row.startNode)"/>
          </template>
        </el-table-column>
        <el-table-column width="auto" label="类型">
          <template #default="scope">
            <el-tag>
              {{ globalData.relationshipTypeNameMap(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column width="auto" label="终止节点">
          <template #default="scope">
            <popover-node-director :node="nodeMap.get(scope.row.endNode)"/>
          </template>
        </el-table-column>
        <el-table-column width="auto" label="属性">
          <template #default="scope">
            <property-popover-button :properties="scope.row.properties"/>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <span v-if="!hasNodeData && !hasRelationshipData">没有数据</span>
  </el-card>
</template>

<script>
import {defineAsyncComponent} from 'vue'

const PopoverNodeDirector = defineAsyncComponent(() => import('../../node/PopoverNodeDirector.vue')),
    PropertyPopoverButton = defineAsyncComponent(() => import('../../node/PropertyPopoverButton.vue'))

export default {
  name: 'GraphDataTableCard',
  components: {
    PropertyPopoverButton,
    PopoverNodeDirector,
  },
  data() {
    return {
      nodeMap: new Map(),
    }
  },
  props: {
    cardId: Number,
    graphData: {
      type: Object,
      default: () => {
        return []
      },
    },
    cardTitle: {
      type: String,
      default: null,
    },
  },
  created() {
    this.graphData.nodes.forEach(node => {
      this.nodeMap.set(node.id, node)
    })
  },
  methods: {
    hasNodeData() {
      return this.graphData.nodes && this.graphData.nodes.length > 0
    },

    hasRelationshipData() {
      return this.graphData.relationships && this.graphData.relationships.length > 0
    },

    clickCloseButton() {
      this.$emit('card-close', this.cardId)
    },

    mapTagName(tag) {
      return this.globalData.nodeLabelNameMap(tag)
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

.table {
  margin-left: auto;
  margin-right: auto;
}

/*noinspection CssUnusedSymbol*/
.el-tag + .el-tag {
  margin-left: 10px;
  margin-bottom: 10px;
}

</style>