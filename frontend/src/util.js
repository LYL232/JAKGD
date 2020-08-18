export default {
  /**
   * 获取简单的节点描述标题
   * @param node 节点对象
   * @returns {string} 标题
   */
  getNodeTitle(node) {
    let name = node.properties.name
    if (!name) {
      name = node.properties['名字']
    }
    return '(' + node.id + ')' + (name ? name : '')
  },
  /**
   * 默认错误提示
   * @param err 错误对象
   * @param hint 出错场景提示
   */
  errorHint(err, hint) {
    if (err.errorMsg) {
      this.$message.error(hint + ': ' + err.errorMsg)
    } else {
      this.$message.error(hint + ': 未知错误')
    }
  },
  /**
   * 默认简单的错误handler
   * @param hint 出错场景提示
   * @returns {function(...[*]=)} handler
   */
  simpleErrorHandler(hint) {
    return err => {
      if (err.errorMsg) {
        this.$message.error(hint + ': ' + err.errorMsg)
      } else {
        this.$message.error(hint + ': 未知错误')
      }
    }
  },
}