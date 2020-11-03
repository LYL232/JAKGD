<template>
  <router-view/>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      activeIndex: '1',
    }
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
