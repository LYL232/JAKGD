<template>
  <el-dialog :title="title" :visible.sync="visible" width="750px" center>
    <el-form v-if="node" ref="form" :model="form"
             label-width="150px" style="width: 95%">
      <el-form-item label="id">
        <el-input placeholder="关系到的节点 id" v-model="form.targetId" clearable></el-input>
      </el-form-item>
      <el-form-item label="关系类型">
        <el-autocomplete
                v-model="form.relType"
                style="width: 100%"
                :fetch-suggestions="fetchSuggestions"
                placeholder="允许中英文数字组合, 不允许数字开头"
                :maxlength="20" show-word-limit
        ></el-autocomplete>
      </el-form-item>
    </el-form>
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
  name: 'CreateRelationshipDialog',
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
  props: {
    node: {
      type: Object,
      default: null,
    },
  },
  computed: {
    title() {
      if (this.node) {
        let name = this.node.properties.name
        return '添加从(' + this.node.id + '):' + (name ? name : '') + ' 开始的关系'
      }
      return '错误, 节点信息缺失'
    },
  },
  methods: {

    show() {
      this.visible = true
    },

    /**
     * 获取请求数据
     * @returns {{}|null}
     */
    getRequestData() {
      let res = {}
      for (let property of this.$refs.propertyEditor.getFormProperties()) {
        if (!property.value || (property.value = property.value.trim()) === '') {
          this.$message('属性值不能为空: ' + property.name)
          return null
        }
        res[property.name] = property.value
      }
      return res
    },

    clickConfirm() {
      if (!this.node) {
        this.$message.error('节点信息缺失')
        return
      }

      if (!this.globalData.username || this.globalData.username === '') {
        this.$message.warning('请登录')
        return
      }

      let requestData = this.getRequestData()
      if (requestData === null) {
        return
      }

      let targetId = parseInt(this.form.targetId)
      if (isNaN(targetId)) {
        this.$message('id必须为整数')
        return
      }

      if (targetId === this.node.id) {
        this.$message('不允许自关联')
        return
      }

      this.form.relType = this.form.relType.trim()
      if (this.form.relType === '') {
        this.$message('关系类型不能为空')
        return
      }

      if (!(/^[a-zA-Z_\u4E00-\u9FA5]([A-Za-z\u4E00-\u9FA50-9]){1,20}$/.exec(this.form.relType))) {
        this.$message.warning('非法的关系名: 请输入中英文字母, 数字组合的属性名, 长度大于等于2小于等于20')
        return
      }

      this.loading = true
      switch (this.form.relType) {
      case '引用': {
        this.createReferRelationship(targetId, requestData)
        break
      }
      default : {
        this.createDefaultRelationship(this.form.relType, targetId, requestData)
        break
      }
      }
    },

    /**
     * 创建其他类型的关系
     * @param relType 关系类型
     * @param targetId 关系另一个目标id
     * @param requestData 请求体
     */
    createDefaultRelationship(relType, targetId, requestData) {
      this.axios.post('/api/relationship/' + this.node.id + '/' + relType + '/' + targetId,
        requestData).then(() => {
        this.createSuccess()
      }).catch(err => {
        this.createFailed(err)
      })
    },
    /**
     * 创建引用类型的关系
     * @param targetId 关系另一节点id
     * @param requestData 请求体
     */
    createReferRelationship(targetId, requestData) {
      this.axios.post('/api/reference/' + this.node.id + '/' + targetId,
        requestData).then(() => {
        this.createSuccess()
      }).catch(err => {
        this.createFailed(err)
      })
    },

    createSuccess() {
      this.$message({
        message: '关联成功',
        type: 'success',
      })
      this.visible = false
      this.loading = false
      this.form = {
        relType: '',
        targetId: '',
      }
      this.$refs.propertyEditor.clearInput()
      this.$emit('created')
    },

    createFailed(err) {
      this.loading = false
      if (err.errorMsg) {
        this.$message.error(err.errorMsg)
      } else {
        this.$message.error('未知服务器错误')
      }
    },

    fetchSuggestions(queryString, cb) {
      // 调用 callback 返回建议列表的数据
      cb([{value: '引用'}])
    },
  },
}
</script>

<style scoped>

</style>