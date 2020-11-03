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
}
</script>

<style>
  html, body, #app {
    padding: 0;
    margin: 0;
    height: 100%;
  }

</style>
