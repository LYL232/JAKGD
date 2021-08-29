<template>
  <el-container>
    <el-header class="header">
      <el-container style="align-items:center">
        <el-col :span="18">
          <img alt="home-logo" src="../../assets/home-logo.png" id="home-logo" class="header-items"/>
          <el-avatar class="header-items" v-if="username">{{ username }}</el-avatar>
          <el-button v-if="!username || username === ''"
                     class="header-items" type="primary" icon="el-icon-user"
                     @click="clickUserButton" circle/>
          <el-button-group class="header-items" v-if="username">
            <el-button class="header-items" type="primary" icon="el-icon-user"
                       @click="toMe()" round>个人中心
            </el-button>
            <el-button type="primary" icon="el-icon-document-copy"
                       @click="clickMyCreationButton" round/>
            <el-button type="primary" icon="el-icon-circle-plus"
                       @click="clickCreateNodeButton" round/>
            <el-button type="warning" icon="el-icon-circle-close" @click="clickLogoutButton" round>注销
            </el-button>
          </el-button-group>
        </el-col>
        <el-col :span="6">
          <el-input prefix-icon="el-icon-search"
                    class="search-input"
                    placeholder="id, 标签, 摘要"
                    v-model="searchString"
                    clearable>
            <template #append>
              <el-button @click="clickSearchButton"
                         icon="el-icon-search" :loading="searchButtonLoading"/>
            </template>
          </el-input>
        </el-col>
      </el-container>
    </el-header>
    <el-main style="border: 1px solid #eee;">
      <el-tabs v-model="currentTabName" type="card" @tab-remove="removeTab">
        <el-tab-pane :name="mainTabName" :closable=false>
          <template #label>
            <span><i class="el-icon-share"></i> 主页</span>
          </template>
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
    </el-main>
    <el-divider/>
    <el-footer class="footer">
      <p style="font-size: 5px; float: right;">
        ©Copyright 2021 LYL232 版权所有
      </p>
    </el-footer>
    <login-dialog ref="loginDialog"></login-dialog>
    <edit-node-dialog ref="editNodeDialog" @created="nodeCreated"></edit-node-dialog>
  </el-container>
</template>

<script>
import '../main-page.css'
import {defineAsyncComponent} from 'vue'
import {useRouter} from 'vue-router'
// 首页优先加载图展示组件, 该组件在CardList中
import CardList from './HomeCardList.vue'

const MarkdownEditor = defineAsyncComponent(() => import( '../markdown/MarkdownEditor.vue')),
    NodeView = defineAsyncComponent(() => import('../node/NodeView.vue')),
    LoginDialog = defineAsyncComponent(() => import( './LoginDialog.vue')),
    EditNodeDialog = defineAsyncComponent(() => import( '../node/EditNodeDialog.vue'))


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
  setup() {
    const router = useRouter(), toMe = (() => {
      router.push('/me')
    })
    return {
      toMe
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
      if (this.searchString === '') {
        this.axios.get(
            '/api/search', {params: {limit: 25}}
        ).then((response) => {
          this.bus.$emit('addHomeCard', {
            cardData: response.data,
            cardTitle: '',
            cardType: 'graph'
          })
          this.searchButtonLoading = false
        }).catch((err) => {
          this.searchButtonLoading = false
          this.util.errorHint(err, '获取搜索信息失败')
        })
        return
      }
      this.querySearchResultCount(this.searchString)
    },
    querySearchResultCount(key) {
      let queryProperties = JSON.stringify(['description', 'name', 'summary'])
      this.axios.all([
        this.axios.post('/api/search/node/property/count', {key, queryProperties}),
        this.axios.post('/api/search/node/label/count', {key}),
        this.axios.post('/api/search/relationship/property/count', {key, queryProperties}),
        this.axios.post('/api/search/relationship/type/count', {key}),
      ]).then(this.axios.spread((resNodeProperty, resNodeLabel, resRelProperty, resRelType) => {
        let resultCount = resNodeProperty.data + resNodeLabel.data + resRelProperty.data + resRelType.data,
            newHomeCard = {cardTitle: '搜索结果: ' + this.searchString}
        if (resultCount > 35) {
          newHomeCard.cardType = 'search-result-table'
          newHomeCard.cardData = {
            nodeInPropertyCount: resNodeProperty.data,
            nodeInLabelCount: resNodeLabel.data,
            relationshipInPropertyCount: resRelProperty.data,
            relationshipInTypeCount: resRelType.data,
            requestParam: {queryProperties, key},
            key,
          }
          this.bus.$emit('addHomeCard', newHomeCard)
          this.searchButtonLoading = false
        } else {
          newHomeCard.cardType = 'graph'
          let skip = 0, limit = 1000, requestList = []
          if (resNodeProperty.data > 0) {
            requestList.push(this.axios.post(
                '/api/search/node/property',
                {key: this.searchString, queryProperties, skip, limit}
            ))
          }
          if (resNodeLabel.data > 0) {
            requestList.push(this.axios.post(
                '/api/search/node/label',
                {key: this.searchString, skip, limit}
            ))
          }
          if (resRelProperty.data > 0) {
            requestList.push(this.axios.post(
                '/api/search/relationship/property',
                {key: this.searchString, queryProperties, skip, limit}
            ))
          }
          if (resRelType.data > 0) {
            requestList.push(this.axios.post(
                '/api/search/relationship/type',
                {key: this.searchString, skip, limit}
            ))
          }
          if (requestList.length === 0) {
            newHomeCard.cardData = {nodes: [], relationships: []}
            this.bus.$emit('addHomeCard', newHomeCard)
            this.searchButtonLoading = false
          } else {
            this.axios.all(requestList).then((resList) => {
              let nodes = [], relationships = [], nodeIdSet = new Set(), relationshipIdSet = new Set()
              resList.forEach(res => {
                if (res.data.nodes) {
                  res.data.nodes.forEach(node => {
                    if (!nodeIdSet.has(node.id)) {
                      nodeIdSet.add(node.id)
                      nodes.push(node)
                    }
                  })
                }
                if (res.data.relationships) {
                  res.data.relationships.forEach(rel => {
                    if (!relationshipIdSet.has(rel.id)) {
                      relationshipIdSet.add(rel.id)
                      relationships.push(rel)
                    }
                  })
                }
              })
              newHomeCard.cardData = {nodes, relationships}
              this.bus.$emit('addHomeCard', newHomeCard)
              this.searchButtonLoading = false
            }).catch(err => {
              this.searchButtonLoading = false
              this.util.errorHint(err, '获取搜索结果数据信息失败')
            })
          }
        }
      })).catch((err) => {
        this.searchButtonLoading = false
        this.util.errorHint(err, '获取搜索结果个数信息失败')
      })
    },
    clickMyCreationButton() {
      let requestBody = {
        key: this.globalData.user.username,
        queryProperties: "[\"author\"]"
      }
      this.axios.all([
        this.axios.post('/api/search/node/property/count', requestBody),
        this.axios.post('/api/search/relationship/property/count', requestBody)
      ]).then(this.axios.spread((nodeRes, relRes) => {
        this.bus.$emit('addHomeCard', {
          cardTitle: '我创建的实体',
          cardData: {
            nodeCount: nodeRes.data,
            relationshipCount: relRes.data,
            requestBody
          },
          cardType: 'mine-creation'
        })
      })).catch(err => {
        this.util.errorHint(err, '获取我创建的实体个数信息失败')
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
  },
  components: {
    EditNodeDialog,
    LoginDialog,
    CardList, MarkdownEditor, NodeView,
  },
}
</script>

<style scoped>
.search-input {
  width: auto;
  height: 40px;
}
</style>