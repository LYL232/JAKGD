<template>
  <div v-if="node.labels">
    <i class="el-icon-collection-tag item"/>
    <el-tag :key="'node-' + node.id + '-label-' + tag"
            v-for="(tag, i) in node.labels"
            :type="colors[i % colors.length]"
            size="medium"
            class="item tag">
      {{ globalData.nodeLabelNameMap(tag) }}
    </el-tag>
    <el-tag v-if="node.labels.length === 0" :type="colors[0]" size="medium" style="font-size: 20px">
      无标签
    </el-tag>
  </div>
</template>

<script>
export default {
  name: 'TagList',
  props: {
    node: {
      type: Object,
      default: null,
    }
  },
  data() {
    return {
      colors: ['success', 'primary', 'info', 'danger', 'warning'],
    }
  }
}
</script>

<style scoped>

.item + .item {
  margin-left: 10px;
}

.tag {
  font-size: 20px;
  font-weight: bold;
}
</style>