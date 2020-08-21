<template>
  <mavon-editor
          :value="mdContent"
          :subfield="false"
          :defaultOpen="'preview'"
          :toolbarsFlag="false"
          :editable="false"
          :scrollStyle="true"
          :ishljs="true"
  />
</template>

<script>

export default {
  name: 'MarkdownBlock',
  props: {
    mdContent: String,
  },
  mounted() {
    // 重新加载latex语法
    if (window.MathJax.typesetPromise) {
      window.MathJax.typesetPromise()
    }
  },
  updated() {
    // 重新加载latex语法
    window.MathJax.typesetPromise()
  },
}
</script>

<style scoped>
</style>