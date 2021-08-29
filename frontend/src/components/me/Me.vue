<template>
  <el-container>
    <el-header class="header">
      <el-container style="align-items:center">
        <img alt="home-logo" src="../../assets/home-logo.png" id="home-logo"/>
        <el-avatar class="header-items" v-if="username">{{ username }}</el-avatar>
        <el-button v-if="!username || username === ''"
                   class="header-items" type="primary" icon="el-icon-user" circle/>
        <el-button-group class="header-items" v-if="username">
          <el-button class="header-items" type="primary" icon="el-icon-s-home"
                     @click="toHome" round>主页
          </el-button>
          <el-button type="warning" icon="el-icon-circle-close" @click="clickLogoutButton" round>注销
          </el-button>
        </el-button-group>
      </el-container>
    </el-header>
    <el-container style="border: 1px solid #eee;">
      <el-aside width="200px" style="background-color: rgb(238, 241, 246)">
        <el-menu
            :default-openeds="['utilities', 'settings']" default-active="settings-change-password"
            @select="menuSelectIndex"
        >
          <el-sub-menu index="utilities">
            <template #title><i class="el-icon-menu"></i>功能</template>
            <el-menu-item-group>
              <template #title><i class="el-icon-user"/>我的</template>
            </el-menu-item-group>
          </el-sub-menu>
          <el-sub-menu index="settings">
            <template #title><i class="el-icon-setting"></i>设置</template>
            <el-menu-item-group>
              <template #title>个人设置</template>
              <el-menu-item index="change-password">修改密码</el-menu-item>
            </el-menu-item-group>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-main>
        <change-password-page v-if="menuIndex === 'change-password'"/>
      </el-main>
    </el-container>
    <el-divider/>
    <el-footer class="footer">
      <p style="font-size: 5px; float: right;">
        ©Copyright 2021 LYL232 版权所有
      </p>
    </el-footer>

  </el-container>
</template>

<script>
import '../main-page.css'
import {defineAsyncComponent} from 'vue'
import {useRouter} from 'vue-router'

const ChangePasswordPage = defineAsyncComponent(() => import( './ChangePasswordPage.vue'))

export default {
  name: 'Me',
  data() {
    return {
      username: null,
      menuIndex: 'change-password'
    }
  },
  setup() {
    const router = useRouter(),
        toHome = (() => {
          router.push('/home')
        })
    return {
      toHome
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
        that.toHome()
      }, 2000)
    }
  },

  methods: {
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
        this.toHome()
      }).catch(() => {
      })
    },
    menuSelectIndex(index, item) {
      this.menuIndex = index
    },
  },
  components: {
    ChangePasswordPage
  }
}
</script>

<style scoped>

</style>