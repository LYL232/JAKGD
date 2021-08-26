<template>
  <el-popover placement="left" :width="width" trigger="click">
    <el-table v-if="properties" :data="propertiesData">
      <el-table-column width="auto" property="name" label="属性"/>
      <el-table-column width="auto" property="value" label="值"/>
    </el-table>
    <span v-if="!properties">没有数据</span>
    <template #reference>
      <el-button round type="primary" icon="el-icon-document"/>
    </template>
  </el-popover>

</template>

<script>
export default {
  name: 'PropertyPopoverButton',
  props: {
    properties: {
      type: Object,
      default: null,
    },
    width: {
      type: Number,
      default: 600,
    },
  },
  computed: {
    propertiesData() {
      let res = []
      for (let name in this.properties) {
        if (Object.prototype.hasOwnProperty.call(this.properties, name)) {
          res.push({
            name: this.globalData.propertyNameMap(name),
            value: this.properties[name],
          })
        }
      }
      return res
    },
  },
}
</script>

<style scoped>

</style>