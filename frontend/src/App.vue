<template>
  <home/>
</template>

<script>
const Home = () => import('./components/home/Home')
export default {
  name: 'App',
  data() {
    return {
      activeIndex: '1',
    }
  },
  components: {Home,},
  created() {
    let that = this
    this.bus.$on('authorizationExpired', () => {
      that.bus.$emit('authorizationChange', {
        username: null,
        authorization: null,
      })
    })
  },
}
</script>

<style>
  html, body, #app {
    padding: 0;
    margin: 0;
    height: 100%;
  }

</style>
