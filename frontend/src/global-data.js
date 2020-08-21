export default {
  /**
   * 节点label名称映射
   * @param label
   * @returns {string|*}
   */
  nodeLabelNameMap(label) {
    if (!label) {
      return '未定义'
    }
    switch (label) {
    case '':
      return '未知'
    case '__knowledge':
      return '知识'
    }
    return label
  },
  /**
   * 属性名称映射
   * @param name
   * @returns {string|*}
   */
  propertyNameMap(name) {
    switch (name) {
    case 'author':
      return '作者'
    case 'name':
      return '名字'
    case 'created':
      return '创建时间'
    case 'updated':
      return '上一次修改时间'
    case 'summary':
      return '摘要'
    case 'description':
      return '描述'
    case 'type':
      return '类型'
    default:
      return name
    }
  },
  /**
   * 关系类型名称映射
   * @param type
   * @returns {string|*}
   */
  relationshipTypeNameMap(type) {
    switch (type) {
    case '__refer': {
      return '引用'
    }
    }
    return type
  },
  // 响应代码对应的信息 index => msg
  responseCodeMessage: [
    '未知错误, 需要查看服务器日志',
    '未认证, 请登录',
    '没有操作权限',
    '用户名已经被占用',
    '请求json格式错误',
    '不能上传空文件',
    '服务器文件处理错误',
    '请求文件不存在',
    '找不到对象',
    '服务器无法连接数据库, 请联系管理员',
    '必要参数缺失',
    '不允许的节点标签',
    '上传的内容过长',
    '无效的参数',
    '不能上传空内容',
    '不能删除有关系的节点',
    '不能创建自引用节点',
    '不能创建重复引用关系',
    '查询执行失败',
    '错误的请求方法',
    '错误的请求文件类型',
    '不允许多重同类型关系',
  ],
  // 不能被用户操作的属性
  internalProperties: new Set(['created', 'updated', 'author', 'labels']),
  // 基础类型节点所需必要属性
  nodeBaseTypeRequireProperties: {
    '__knowledge': new Set(['name', 'summary']),
  },
  // 基础类型关系所需必要属性
  relationshipBaseTypeRequireProperties: {}
}