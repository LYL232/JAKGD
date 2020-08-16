<template>
  <el-container direction="horizontal">
    <div v-for="item in properties" :key="item.name">
      <el-popover placement="top" trigger="click">
        <p>{{item.value}}</p>
        <el-button style="margin-left: 10px;" slot="reference" round>
          {{item.name}}
        </el-button>
      </el-popover>
    </div>
  </el-container>
</template>

<script>
export default {
  name: 'PropertiesBox',
  props: {
    properties: Array,
  },
}
</script>

<style scoped>

</style>