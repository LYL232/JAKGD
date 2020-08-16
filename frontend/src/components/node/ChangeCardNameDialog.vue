<template>
  <el-dialog title="修改卡片名" :visible.sync="visible" width="35%" center>
    <el-form ref="form" :model="form" label-width="80px" style="width: 95%">
      <el-form-item label="卡片名">
        <el-input v-model="form.name" maxlength="20" show-word-limit></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="clickConfirm" :loading="loading">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>

export default {
  name: 'ChangeCardNameDialog',
  data() {
    return {
      visible: false,
      loading: false,
      form: {
        name: '',
      },
      documentId: -1,
    }
  },
  methods: {
    /**
     * @param id 需要编辑的document id
     * @param name 原先的name
     */
    show(id, name) {
      this.visible = true
      this.documentId = id
      this.form.name = name
    },
    clickConfirm() {
      this.loading = true
      this.axios.put('/api/document/' + this.documentId,
        {name: this.form.name}).then(() => {
        this.$message({
          message: '修改成功',
          type: 'success',
        })
        this.visible = false
        this.loading = false
        this.$emit('updated', {
          name: this.form.name,
          docId: this.documentId
        })
      }).catch(err => {
        this.util.errorHint(err, '修改卡片名字失败')
        this.loading = false
      })
    },
  },
}
</script>

<style scoped>

</style>