<template>
  <el-dialog title="登录" v-model="visible" width="30%" center>
    <el-form ref="form" :model="form" label-width="80px" style="width: 95%">
      <el-form-item label="用户名">
        <el-input v-model="form.username"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input show-password type="password" v-model="form.password"></el-input>
      </el-form-item>
    </el-form>
    <template #footer class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="clickConfirm" :loading="loading">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: 'LoginDialog',
  data() {
    return {
      form: {
        username: '',
        password: '',
      },
      visible: false,
      loading: false,
    }
  },
  methods: {
    show() {
      this.visible = true
    },
    clickConfirm() {
      if (this.form.username === '' || this.form.password === '') {
        this.$notify.info({
          title: '输入错误',
          message: '用户名或密码不能为空',
        })
        return
      }
      this.loading = true
      this.axios.post('/login', {
        username: this.form.username,
        password: this.form.password,
      }).then((response) => {
        this.loading = false
        this.visible = false
        this.bus.$emit('authorizationChange', {
          username: this.form.username,
          authorization: response.headers['authorization'],
        })
        this.$notify({
          title: '成功',
          type: 'success',
          message: '登录成功',
        })
      }).catch((err) => {
        this.loading = false
        this.util.errorHint(err, '登录失败')
      })
    },
  },
}
</script>

<style scoped>

</style>