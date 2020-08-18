<template>
  <el-dialog :title="title" :visible.sync="visible" width="750px" center>
    <property-editor ref="propertyEditor"/>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="clickConfirm" :loading="loading">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import PropertyEditor from './PropertyEditor'

export default {
  name: 'EditRelationshipDialog',
  components: {PropertyEditor},
  data() {
    return {
      form: {
        targetId: '',
        relType: '',
      },
      visible: false,
      loading: false,
      relationship: null,
    }
  },
  computed: {
    title() {
      if (this.relationship) {
        return '编辑关系: (' + this.relationship.startNode + ')-[' +
          this.globalData.relationshipTypeNameMap(this.relationship.type) +
          ']->(' + this.relationship.endNode + ') 的属性'
      } else {
        return '关系信息缺失'
      }
    },
  },
  methods: {
    show(relationship) {
      if (relationship) {
        this.visible = true
        this.relationship = relationship
        this.$nextTick(() => {
          this.$refs.propertyEditor.clearInput()
          for (let name in relationship.properties) {
            if (Object.prototype.hasOwnProperty.call(relationship.properties, name)) {
              this.addNewPropertyInput(name, relationship.properties[name])
            }
          }
        })
      } else {
        this.$notify.error({
          title: '错误',
          message: '无法获取编辑的关系信息',
        })
      }
    },

    clickConfirm() {
      let requestData = this.getRequestData()
      if (requestData === null) {
        return
      }
      this.updateDefaultRelationship(requestData)
    },

    /**
     * 添加新的属性输入框
     * @param name 属性名
     * @param value 初始属性值
     */
    addNewPropertyInput(name, value) {
      let deletable = true,
        relationshipBaseTypeRequireProperties =
          this.globalData.relationshipBaseTypeRequireProperties
      let relType = this.relationship.type
      if (relType.startsWith('__')
        && relationshipBaseTypeRequireProperties[relType]
        && relationshipBaseTypeRequireProperties[relType].has(name)) {
        deletable = false
      }
      this.$refs.propertyEditor.addNewPropertyInput(name, value, deletable, false)
    },

    /**
     * 获取请求数据
     * @returns {{}|null}
     */
    getRequestData() {
      let res = {}, propertyEditor = this.$refs.propertyEditor
      for (let property of propertyEditor.getFormProperties()) {
        if (!property.value || (property.value = property.value.trim()) === '') {
          this.$notify.info({
            title: '输入错误',
            message: '属性值不能为空: ' + property.name,
          })
          return null
        }
        res[property.name] = property.value
      }
      return res
    },

    /**
     * 默认关系更新动作
     * @param requestData 请求体
     */
    updateDefaultRelationship(requestData) {
      this.loading = true
      this.axios.put('/api/relationship/' + this.relationship.id, requestData).then(() => {
        this.$notify({
          title: '成功',
          type: 'success',
          message: '修改关系成功',
        })
        this.finishRequest()
        for (let name in requestData) {
          if (Object.prototype.hasOwnProperty.call(requestData, name)) {
            this.relationship.properties[name] = requestData[name]
          }
        }
        this.$emit('updated', this.relationship)
      }).catch(err => {
        this.util.errorHint(err, '修改关系失败')
        this.finishRequest()
      })
    },
    finishRequest() {
      this.loading = false
      this.visible = false
    },
  },
}
</script>

<style scoped>
</style>