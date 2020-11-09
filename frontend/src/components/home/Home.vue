<template>
  <div>
    <el-container class="header">
      <el-container>
        <img alt="home-logo" src="../../assets/home-logo.png" id="home-logo"/>
        <el-avatar class="header-items" v-if="username">{{username}}</el-avatar>
        <el-button v-if="!username || username === ''"
                   class="header-items" type="primary" icon="el-icon-user"
                   @click="clickUserButton" circle/>
        <el-button-group class="header-items" v-if="username">
          <el-button class="header-items" type="primary" icon="el-icon-setting"
                     @click="clickToMeButton" round/>
          <el-button type="primary" icon="el-icon-circle-plus"
                     @click="clickCreateNodeButton" round/>
          <el-button type="warning" @click="clickLogoutButton" round>注销
          </el-button>
        </el-button-group>
      </el-container>
      <el-input prefix-icon="el-icon-search" class="search-input header-items" placeholder="id, 标签, 摘要"
                v-model="searchString"
                clearable>
        <el-button @click="clickSearchButton" slot="append"
                   icon="el-icon-search" :loading="searchButtonLoading"/>
      </el-input>

    </el-container>
    <div>
      <el-tabs v-model="currentTabName" type="card" @tab-remove="removeTab">
        <el-tab-pane
            :name="mainTabName"
            :closable=false
        >
          <span slot="label"><i class="el-icon-share"></i> 主页</span>
          <card-list/>
        </el-tab-pane>
        <el-tab-pane
            v-for="item in tabs"
            :key="'tab-' + item.name"
            :label="item.label"
            :name="item.name"
            :closable=true>
          <markdown-editor v-if="item.type === 'md-editor'" :doc="item.doc"
                           :header="item.header" :node="item.node"/>
          <node-view v-if="item.type === 'node-view'" :node-id="item.node.id"/>
        </el-tab-pane>
      </el-tabs>
      <el-divider/>
      <el-footer class="footer">
        <p style="font-size: 5px; float: right;">
          ©Copyright 2020 LYL232 版权所有
        </p>
      </el-footer>
    </div>
    <login-dialog ref="loginDialog"></login-dialog>
    <edit-node-dialog ref="editNodeDialog" @created="nodeCreated"></edit-node-dialog>
  </div>
</template>

<script>
// 首页优先加载图展示组件, 该组件在CardList中
import CardList from './HomeCardList'

const MarkdownEditor = () => import( '../markdown/MarkdownEditor'),
  NodeView = () => import('../node/NodeView'),
  LoginDialog = () => import( './LoginDialog'),
  EditNodeDialog = () => import( '../node/EditNodeDialog')

export default {
  name: 'Main',
  data() {
    return {
      tabs: [],
      // 当前选项卡的名字
      currentTabName: 'main-tab',
      // 当前选项卡的下标
      tabIndex: 0,
      // 搜索输入框的字符串
      searchString: '',
      username: null,
      loading: false,
      searchButtonLoading: false,
      mainTabName: 'main-tab',
    }
  },
  created() {
    this.bus.$on('newTab', tabConfig => {
      this.addTab(tabConfig)
    })
    this.bus.$on('removeTab', tabConfig => {
      if (tabConfig.type === 'node-view') {
        for (let i = 0; i < this.tabs.length; ++i) {
          let tab = this.tabs[i]
          if (tab.node.id === tabConfig.node.id) {
            this.removeTab(tab.name)
            return
          }
        }
      }
    })
    this.bus.$on('homeCardAdded', () => {
      this.currentTabName = this.mainTabName
    })
    this.bus.$on('authorizationChange', ({username, authorization}) => {
      if (!username || username === '' || !authorization) {
        this.globalData.user = null
        this.username = null
        return
      }
      this.globalData.user = {
        username, authorization
      }
      this.username = username
    })
  },
  mounted() {
    if (this.globalData.user) {
      this.username = this.globalData.user.username
    } else {
      this.username = null
    }
    this.clickSearchButton()
  },
  methods: {
    //
    // 增加一个可关闭的选项卡
    // @param tabData: 选项卡的数据对象: {
    // label: 选项卡标题,
    // type: 类型: ['md-editor', 'node-view']
    // doc: 文档数据, 在type属于 {'md-editor'} 时有效
    // node: 节点数据, 在type属于 {'node-view'} 时有效
    // }
    addTab(tabData) {

      switch (tabData.type) {
      case 'node-view': {
        tabData.label = this.util.getNodeTitle(tabData.node)
        break
      }
      case 'md-editor': {
        tabData.label = this.util.getNodeTitle(tabData.node) + '.' + tabData.doc.name
        tabData.header = '编辑文档:' + tabData.label
        break
      }
      }

      let newTabName = ++this.tabIndex + ''
      tabData.name = newTabName
      if (!tabData.label || tabData.label === '') {
        tabData.label = newTabName
      }
      if (tabData.type === 'node-view') {
        for (let i = 0; i < this.tabs.length; ++i) {
          let tab = this.tabs[i]
          if (tab.node && tab.node.id === tabData.node.id) {
            this.currentTabName = tab.name
            return
          }
        }
      }
      this.tabs.push(tabData)
      this.currentTabName = newTabName
    },
    /**
     * 根据tab的名字(唯一)删除指定tab
     * @param targetName 删除的名字
     */
    removeTab(targetName) {
      let tabs = this.tabs,
        currentTabName = this.currentTabName
      if (currentTabName === targetName) {
        tabs.forEach((tab, index) => {
          if (tab.name === targetName) {
            let nextTab = tabs[index + 1] || tabs[index - 1]
            if (nextTab) {
              currentTabName = nextTab.name
            }
          }
        })
      }

      this.tabs = tabs.filter(tab => tab.name !== targetName)
      if (this.tabs.length === 0) {
        this.tabIndex = 0
        this.currentTabName = this.mainTabName
      } else {
        this.currentTabName = currentTabName
      }
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
      }).catch(() => {
      })

    },
    clickSearchButton() {
      this.searchButtonLoading = true
      this.axios.post('/api/search', {
        key: this.searchString,
        limit: (!this.searchString || this.searchString === '') ? 25 : 1000,
        queryProperties: JSON.stringify(['description', 'name', 'summary']),
      }).then(response => {
        this.searchButtonLoading = false
        let data = response.data, totLength = data.nodes.length,
          newHomeCard = {
            cardData: data,
            cardTitle: this.searchString !== '' ? '搜索结果: ' + this.searchString : '',
          }
        if (totLength > 35 && this.searchString && this.searchString !== '') {
          newHomeCard.cardType = 'graph-data-table'
        } else {
          newHomeCard.cardType = 'graph'
        }

        this.bus.$emit('addHomeCard', newHomeCard)
      }).catch(err => {
        this.searchButtonLoading = false
        this.util.errorHint(err, '获取搜索信息失败')
      })
    },
    nodeCreated(nodeId) {
      this.axios.get('/api/node/' + nodeId).then(response => {
        this.addTab({type: 'node-view', node: response.data})
      }).catch(err => {
        this.util.errorHint(err, '获取节点数据失败')
      })
    },
    clickCreateNodeButton() {
      this.$refs.editNodeDialog.show()
    },
    clickUserButton() {
      this.$refs.loginDialog.show()
    },
    clickToMeButton() {
      this.$router.push('/me')
    }
  },
  components: {
    EditNodeDialog,
    LoginDialog,
    CardList, MarkdownEditor, NodeView,
  },
}
</script>

<style scoped>
  .header {
    box-shadow: lightblue 0 0 0 1px;
    height: auto;
    padding: 20px 15%;
  }

  .search-input {
    width: auto;
    height: 40px;
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