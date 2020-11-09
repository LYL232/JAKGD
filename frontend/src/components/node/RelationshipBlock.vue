<template>
  <div style="align-items: center; padding-left: 10px">
    <popover-node-director
            v-for="(each, i) in asEndNode" :key="'as-end-node-' + i"
            @relationship-deleted="$emit('relationship-deleted')"
            @edit-relationship="editRelationship"
            :node="each.startNode" :relationship="each.relationship"/>
    <div v-if="asEndNode.length > 0" class="relationship-type">
      <h3 style="display: inline">{{type}}</h3>
    </div>
    <span v-if="node.properties.name" style="font-weight: bold;">
      (this)
    </span>
    <div v-if="asStartNode.length > 0" class="relationship-type">
      <h3 style="display: inline">{{type}}</h3>
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
  .relationship-type {
    display: inline;
    margin-left: 5px;
    margin-right: 5px;
  }
</style>