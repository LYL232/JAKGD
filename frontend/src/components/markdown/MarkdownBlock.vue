<template>
  <mavon-editor
          :value="content"
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
  computed: {
    content() {
      return this.mdContent.replace('{{JAKGD_BACKEND_URL}}', window.location.origin)
    }
  },
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