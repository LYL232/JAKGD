<template>
  <el-container style="margin-bottom: 40px;" v-loading="loading" direction="vertical">
    <change-card-name-dialog ref="changeCardNameDialog" @updated="cardNameUpdated"/>
    <el-card class="card" v-for="(item, i) in cards"
             :key="'node-' + node.id + '-card-' + item.id">
      <template #header>
        <h2 v-if="item.header" style="display: inline">{{ item.header }}</h2>
        <el-button icon="el-icon-edit" circle style="margin-left: 20px;"
                   type="primary"
                   @click="clickCardNameEditorButton(item.id)"></el-button>
        <el-button-group style="float:right">
          <el-button type="primary" icon="el-icon-arrow-up" circle :loading="docButtonLoading"
                     @click="clickUpwardButton(item.id)" :disabled="i === 0"/>
          <el-button type="primary" icon="el-icon-arrow-down" circle :loading="docButtonLoading"
                     @click="clickDownwardButton(item.id)" :disabled="i === cards.length - 1"/>
          <el-button type="primary" icon="el-icon-edit-outline" circle
                     @click="clickEditButton(i)"/>
          <el-button type="danger" icon="el-icon-delete" circle :loading="docButtonLoading"
                     @click="clickDeleteButton(item.id)"/>
        </el-button-group>
        <el-popover placement="right" width="400" trigger="click">
          <el-table :data="item.properties">
            <el-table-column width="150" property="name" label="属性"/>
            <el-table-column width="250" property="value" label="值"/>
          </el-table>
          <template #reference>
            <el-button style="margin-left: 10px"
                       type="primary" icon="el-icon-document" circle/>
          </template>
        </el-popover>
      </template>
      <markdown-block v-if="item.cardType === 'markdown'" :md-content="item.content"/>
    </el-card>

  </el-container>
</template>

<script>
import {defineAsyncComponent} from 'vue'
import {stringify} from 'qs'

const MarkdownBlock = defineAsyncComponent(() => import('../markdown/MarkdownBlock.vue')),
    ChangeCardNameDialog = defineAsyncComponent(() => import('./ChangeCardNameDialog.vue'))

export default {
  name: 'NodeCardList',
  data() {
    return {
      cards: [],
      loading: false,
      docButtonLoading: false,
    }
  },
  props: {
    node: Object,
  },
  components: {
    ChangeCardNameDialog,
    MarkdownBlock,
  },
  mounted() {
    if (this.node) {
      this.fetchCardsData()
    }
  },
  methods: {
    noCards() {
      return this.cards.length === 0
    },
    fetchCardsData() {
      this.loading = true
      this.cards = []
      this.axios.get('/api/document/partOf/' + this.node.id).then(response => {
        let data = response.data
        data.forEach(doc => {
          this.cards.push({
            header: doc.name,
            cardType: doc.type,
            content: doc.content,
            id: doc.id,
            properties: [
              {
                name: 'id',
                value: doc.id,
              }, {
                name: '作者',
                value: doc.author,
              }, {
                name: '类型',
                value: doc.type,
              }, {
                name: '创建时间',
                value: doc.created,
              }, {
                name: '最后更新时间',
                value: doc.updated,
              },
            ],
          })
        })
        this.loading = false
      }).catch(err => {
        if (err.errorMsg) {
          this.$notify.error({
            title: '数据获取错误',
            message: '获取卡片数据失败: + err.errorMsg',
          })

        } else {
          this.$notify.error({
            title: '数据获取错误',
            message: '获取卡片数据失败: 未知错误',
          })
        }
        this.loading = false
      })
    },
    clickEditButton(index) {
      let card = this.cards[index]
      this.bus.$emit('newTab', {
        doc: {
          content: card.content,
          name: card.header,
          id: card.id,
        },
        node: this.node,
        type: 'md-editor',
      })

    },
    clickDeleteButton(id) {
      this.$confirm('永久删除该卡片? 该操作不可撤回', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        this.docButtonLoading = true
        this.axios.delete('/api/document/' + id).then(() => {
          this.$notify({
            title: '成功',
            message: '删除成功',
            type: 'success',
          })
          this.docButtonLoading = false
          this.fetchCardsData()
        }).catch(err => {
          this.util.errorHint(err, '删除卡片失败')
          this.docButtonLoading = false
        })
      }).catch(() => {
      })
    },
    clickUpwardButton(id) {
      this.requestDocumentSwitch(id, true)
    },
    clickDownwardButton(id) {
      this.requestDocumentSwitch(id, false)
    },
    /**
     * 请求服务器交换Document的位置
     * @param id document id
     * @param upward 是否是向上交换
     */
    requestDocumentSwitch(id, upward) {
      for (let i = 0; i < this.cards.length; ++i) {
        if (this.cards[i].id === id) {
          if (upward) {
            if (i === 0) {
              return
            }
          } else {
            if (i === this.cards.length - 1) {
              return
            }
          }
          break
        }
      }

      this.docButtonLoading = true
      this.axios.post('/api/document/' + id + '/partOf/' + this.node.id + '/switch',
          stringify({type: upward ? 'upward' : 'downward'}),
      ).then(() => {
        this.docButtonLoading = false
        this.switchCard(id, upward)
      }).catch(err => {
        this.util.errorHint(err, '请求交换位置失败')
        this.docButtonLoading = false
      })
    },
    /**
     * 本地交换卡片的位置
     * @param id 卡片id
     * @param upward 是否向上交换
     */
    switchCard(id, upward) {
      for (let i = 0; i < this.cards.length; ++i) {
        if (this.cards[i].id === id) {
          if (upward) {
            if (i > 0) {
              let card = this.cards[i - 1]
              this.cards[i - 1] = this.cards[i]
              this.cards[i] = card
            }
          } else {
            if (i < this.cards.length - 1) {
              let card = this.cards[i + 1]
              this.cards[i + 1] = this.cards[i]
              this.cards[i] = card
            }
          }
          break
        }
      }
    },
    /**
     * 点击修改卡片名字按钮
     * @param id 卡片id(document id)
     */
    clickCardNameEditorButton(id) {
      for (let card of this.cards) {
        if (card.id === id) {
          this.$refs.changeCardNameDialog.show(card.id, card.header)
          return
        }
      }
    },
    /**
     * 卡片名字更新成功
     * @param docId
     * @param name
     */
    cardNameUpdated({docId, name}) {
      for (let card of this.cards) {
        if (card.id === docId) {
          card.header = name
          return
        }
      }
    },
  },
}
</script>

<style scoped>
.card {
  width: 100%;
  margin-right: auto;
  margin-left: auto;
  margin-top: 10px;
  bottom: 10px;
  padding: 10px;
}
</style>