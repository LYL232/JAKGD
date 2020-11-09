<template>
  <div>
    <el-container class="header">
      <el-container>
        <img alt="home-logo" src="../../assets/home-logo.png" id="home-logo"/>
        <el-avatar class="header-items" v-if="username">{{username}}</el-avatar>
        <el-button v-if="!username || username === ''"
                   class="header-items" type="primary" icon="el-icon-user" circle/>
        <el-button-group class="header-items" v-if="username">
          <el-button class="header-items" type="primary" icon="el-icon-s-home"
                     @click="clickToHomeButton" round/>
          <el-button type="primary" @click="clickChangePasswordButton" round>修改密码
          </el-button>
          <el-button type="warning" @click="clickLogoutButton" round>注销
          </el-button>
        </el-button-group>
      </el-container>
    </el-container>
    <change-password-dialog ref="changePasswordDialog"></change-password-dialog>
  </div>
</template>

<script>
const ChangePasswordDialog = () => import( './ChangePasswordDialog')
export default {
  name: 'Me',
  data() {
    return {
      username: null
    }
  },

  mounted() {
    if (this.globalData.user) {
      this.username = this.globalData.user.username
    } else {
      this.username = null
      this.$notify({
        title: '警告',
        message: '登录信息失效, 2秒后返回主页',
        type: 'warning',
        showClose: false,
        duration: 2000
      })
      let that = this
      setTimeout(() => {
        that.$router.push('/home')
      }, 2000)
    }
  },

  methods: {
    clickToHomeButton() {
      this.$router.push('/home')
    },
    clickChangePasswordButton() {
      this.$refs.changePasswordDialog.show()
    },
    clickLogoutButton() {

      this.$confirm('确认注销?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        // 登出只需将全局认证信息置为空即可
        // 发送认证改变事件, 重置认证头
        this.bus.$emit('authorizationChange', {
          username: null,
          authorization: null,
        })
        this.$router.push('/home')
      }).catch(() => {
      })
    },
  },
  components: {
    ChangePasswordDialog
  }
}
</script>

<style scoped>
  .header {
    box-shadow: lightblue 0 0 0 1px;
    height: auto;
    padding: 20px 15%;
  }

  .header-items {
    margin: auto 5px;
  }

  #home-logo {
    width: 100px;
    height: 75px;
    max-width: 100%;
    max-height: 100%;
  }
</style>