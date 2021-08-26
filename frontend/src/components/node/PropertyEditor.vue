<template>
  <el-form ref="form" :model="form" label-width="150px" style="width: 95%">
    <el-form-item v-for="(item, i) in form.properties" :key="'form-item-' + i"
                  :label="item.label">
      <el-input v-if="item.type === 'text'" type="text"
                :style="item.deletable ? 'width: 75%' : 'width: 100%'"
                v-model="item.value" :maxlength="item.maxLength" show-word-limit/>
      <el-input v-if="item.type === 'textarea'"
                :style="item.deletable ? 'width: 75%' : 'width: 100%'" type="textarea"
                v-model="item.value" :maxlength="item.maxLength"
                :autosize="{ minRows: 4, maxRows: 6}"/>
      <el-button v-if="item.deletable" type="primary" size="small" style="float: right"
                 @click.prevent="removePropertyInput(item.name)"
                 icon="el-icon-delete"/>
    </el-form-item>
    <el-form-item>
      <el-autocomplete
          v-model="newPropertyName" style="width: 75%"
          :fetch-suggestions="fetchSuggestions"
          placeholder="允许中英文数字组合, 不允许数字开头"
          :maxlength="20" show-word-limit clearable
          @select="newPropertyInputSelected"/>
      <el-button style="float: right" type="primary" size="small"
                 @click="clickNewPropertyNameButton">+ 新属性
      </el-button>
    </el-form-item>
  </el-form>

</template>

<script>
export default {
  name: 'PropertyEditor',
  data() {
    return {
      form: {
        properties: [],
      },
      newPropertyName: '',
    }
  },
  methods: {
    fetchSuggestions(queryString, cb) {
      // 调用 callback 返回建议列表的数据
      let res = []
      for (let value of ['名字', '摘要', '描述']) {
        let notExist = true
        for (let exists of this.form.properties) {
          if (this.globalData.propertyNameMap(exists.name) === value) {
            notExist = false
            break
          }
        }
        if (notExist) {
          res.push({value})
        }
      }
      cb(res)
    },
    clearInput() {
      this.form.properties = []
    },

    getFormProperties() {
      return this.form.properties
    },

    removePropertyInput(name) {
      for (let i = 0; i < this.form.properties.length; ++i) {
        if (this.form.properties[i].name === name) {
          this.form.properties.splice(i, 1)
          return
        }
      }
    },

    newPropertyInputSelected() {
      this.clickNewPropertyNameButton()
    },

    clickNewPropertyNameButton() {
      if (!this.newPropertyName
          || (this.newPropertyName = this.newPropertyName.trim()) === '') {
        this.$notify.info({
          title: '输入错误',
          message: '请输入新的属性名',
        })
        return
      }
      this.addNewPropertyInput(this.newPropertyName, '', true, true)
      this.newPropertyName = ''
    },

    /**
     * 添加新的属性输入框
     * @param name 属性名
     * @param value 初始属性值
     * @param deletable 是否可以删除
     * @param info 是否提示信息
     */
    addNewPropertyInput(name, value = '', deletable = true, info = false) {
      if (!(/^[a-zA-Z_\u4E00-\u9FA5]([A-Za-z\u4E00-\u9FA50-9]){1,20}$/.exec(name))) {
        if (info) {
          this.$notify.info({
            title: '输入错误',
            message: '非法的属性名: 请输入中英文字母, 数字组合的属性名, 长度大于等于2小于等于20',
          })
        }
        return
      }

      for (let property of this.form.properties) {
        if (name === property.name
            || name === this.globalData.propertyNameMap(property.name)) {
          property.deletable = deletable
          property.value = value
          if (info) {
            this.$notify.info({
              title: '输入错误',
              message: '属性已经存在: ' + name,
            })
          }
          return
        }
      }

      if (this.globalData.internalProperties.has(name)) {
        if (info) {
          this.$notify.info({
            title: '输入错误',
            message: '不允许设置' + name + '属性',
          })
        }
        return
      }

      // 特殊的属性有特殊的'照顾'
      switch (name) {
      case '名字':
      case 'name': {
        this.form.properties.push({
          name: 'name',
          label: '名字(name)',
          maxLength: 20,
          type: 'text',
          value,
          deletable,
        })
        break
      }
      case '摘要':
      case 'summary': {
        this.form.properties.push({
          name: 'summary',
          label: '摘要(summary)',
          maxLength: 100,
          type: 'textarea',
          value,
          deletable,
        })
        break
      }
      case '描述':
      case 'description': {
        this.form.properties.push({
          name: 'description',
          label: '描述(description)',
          maxLength: 1024,
          type: 'textarea',
          value,
          deletable,
        })
        break
      }
      default: {
        this.form.properties.push({
          name: name,
          label: name,
          maxLength: 64,
          type: 'text',
          value,
          deletable,
        })
        break
      }
      }
    },
  },
}
</script>

<style scoped>

</style>