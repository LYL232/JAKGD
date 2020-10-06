<template>
  <div class="card-view">
    <div v-for="item in cards" :key="item.id">
      <graph-card
              v-if="item.cardType === 'graph'"
              :graph-data="item.cardData"
              :card-id="item.id"
              :card-title="item.cardTitle"
              @card-close="onCardClose"/>
      <graph-data-table-card
              v-if="item.cardType === 'graph-data-table'"
              :card-id="item.id" :graph-data="item.cardData"
              :card-title="item.cardTitle"
              @card-close="onCardClose"/>
    </div>
  </div>
</template>

<script>
// 首页优先加载图展示组件
import GraphCard from './card/GraphCard'

const GraphDataTableCard = () => import( './card/GraphDataTableCard')

export default {
  name: 'CardList',
  data() {
    return {
      // 表示卡片数据的
      // {
      //    id: String
      //    cardType: String  ['graph'] 卡片类型
      //    cardData: Object
      // }
      cards: [],
      // 保持id顺序
      idCount: 0,
    }
  },
  created() {
    this.bus.$on('addHomeCard', (cardConfig) => {
      //添加卡片
      cardConfig.id = this.idCount
      this.cards.unshift(cardConfig)
      this.idCount += 1
      this.bus.$emit('homeCardAdded')
    })
  },
  components: {
    GraphDataTableCard,
    GraphCard,
  },
  methods: {
    /**
     * 用户点击关闭卡片按键
     * @param cardId 被删除的卡片id
     */
    onCardClose(cardId) {
      for (let i = 0; i < this.cards.length; ++i) {
        if (this.cards[i].id === cardId) {
          this.cards.splice(i, 1)
          break
        }
      }
      if (this.cards.length === 0) {
        this.idCount = 0
      }
    },
  },
}
</script>

<style scoped>
  .card-view {
    width: 1200px;
    margin-left: auto;
    margin-right: auto;
    margin-bottom: 40px;
  }
</style>