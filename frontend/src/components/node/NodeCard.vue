<template>
  <el-card class="card">
    <template #header class="clear-fix">
      <h2 v-if="cardData.header" style="display: inline">{{cardData.header}}</h2>
      <el-button-group style="float:right">
        <el-button type="primary" icon="el-icon-document" circle></el-button>
        <el-button type="primary" icon="el-icon-arrow-up" circle></el-button>
        <el-button type="primary" icon="el-icon-arrow-down" circle></el-button>
        <el-button type="primary" icon="el-icon-edit-outline" @click="clickEditButton" circle></el-button>
        <el-button type="danger" icon="el-icon-delete" circle></el-button>
      </el-button-group>
    </template>
    <div v-html="htmlContent"></div>
  </el-card>
</template>

<script>
export default {
  name: 'NodeCard',
  data() {
    return {
      htmlContent: ''
    }
  },
  props: {
    cardData: {
      type: Object,
      default: null,
    },
  },
  methods: {
    clickEditButton() {

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
</style>