<template>
  <el-table v-if="hasData" :data="properties" max-height="900px">
    <el-table-column property="name" label="属性"/>
    <el-table-column property="value" label="值"/>
  </el-table>
</template>

<script>
export default {
  name: 'PropertiesTable',
  props: {
    properties: Array,
  },
  computed: {
    hasData() {
      return this.properties && this.properties.length
    }
  }
}
</script>

<style scoped>

</style>