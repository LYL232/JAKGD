<template>
  <el-table :data="entityData" class="table" v-loading="loading" v-if="entityType === 'relationship'">
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
  <el-table :data="entityData" class="table" v-if="entityType === 'node'" v-loading="loading">
    <el-table-column width="100" property="id" label="id"/>
    <el-table-column width="200" property="properties.name" label="名字"/>
    <el-table-column width="auto" property="properties.summary" label="摘要"/>
    <el-table-column width="200" label="标签">
      <template #default="scope">
        <el-tag :key="tableIdentity + '-' + scope.row.id + tag"
                v-for="tag in scope.row.labels">
          {{ mapTagName(tag) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column width="150" label="属性">
      <template #default="scope">
        <popover-node-director :node="scope.row"/>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import './table-common.css'
import {defineAsyncComponent} from 'vue'

const PopoverNodeDirector = defineAsyncComponent(() => import('../../node/PopoverNodeDirector.vue')),
    PropertyPopoverButton = defineAsyncComponent(() => import('../../node/PropertyPopoverButton.vue'))

export default {
  name: 'EntityTable',
  props: {
    entityType: String,
    tableIdentity: String,
    nodeMap: Map,
    entityData: Array,
    loading: Boolean
  },
  components: {
    PopoverNodeDirector,
    PropertyPopoverButton
  },
  methods: {
    mapTagName(tag) {
      return this.globalData.nodeLabelNameMap(tag)
    },
  }
}
</script>

<style scoped>

</style>