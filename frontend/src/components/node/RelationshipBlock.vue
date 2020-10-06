<template>
  <div style="align-items: center; padding-left: 10px">
    <popover-node-director
            v-for="(each, i) in asEndNode" :key="'as-end-node-' + i"
            @relationship-deleted="$emit('relationship-deleted')"
            @edit-relationship="editRelationship"
            :node="each.startNode" :relationship="each.relationship"/>
    <div v-if="asEndNode.length > 0" style="display: inline">
      <i class="el-icon-d-arrow-right"/>
      <p style="display: inline">{{type}}</p>
      <i class="el-icon-d-arrow-right"/>
    </div>
    <p v-if="node.properties.name" style="display: inline">
      ({{node.id}}):{{node.properties.name}}
    </p>
    <div v-if="asStartNode.length > 0" style="display: inline">
      <i class="el-icon-d-arrow-right"/>
      <p style="display: inline">{{type}}</p>
      <i class="el-icon-d-arrow-right"/>
    </div>
    <popover-node-director
            v-for="(each, i) in asStartNode" :key="'as-start-node-' + i"
            @relationship-deleted="$emit('relationship-deleted')"
            @edit-relationship="editRelationship"
            :node="each.endNode" :relationship="each.relationship"/>
    <el-divider/>
  </div>
</template>

<script>
const PopoverNodeDirector = () => import('./PopoverNodeDirector')

export default {
  name: 'RelationshipBlock',
  props: {
    node: Object,
    asStartNode: Array,
    asEndNode: Array,
    type: String,
  },
  components: {
    PopoverNodeDirector,
  },
  methods: {
    /**
     * 转发子组件PopoverNodeDirector的点击编辑关系按钮事件
     * @param relationship
     */
    editRelationship(relationship) {
      this.$emit('edit-relationship', relationship)
    },
  },
}
</script>

<style scoped>

</style>