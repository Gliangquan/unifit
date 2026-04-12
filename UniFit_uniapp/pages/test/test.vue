<template>
  <view class="uf-page">
    <template v-if="isAdminRole">
      <view class="uf-card uf-fade-up">
        <view class="row-between">
          <view class="uf-section-title" style="margin: 0">体测管理入口</view>
          <view class="uf-pill">管理员</view>
        </view>
        <view class="admin-desc">管理员账号不录入个人体测成绩，请在此维护体测标准并管理训练模板。</view>
        <view class="item-grid">
          <view class="admin-tile" @click="go('/pages/admin/standards')">体测标准维护</view>
          <view class="admin-tile" @click="go('/pages/admin/templates')">计划模板维护</view>
          <view class="admin-tile" @click="go('/pages/admin/exercises')">动作库维护</view>
          <view class="admin-tile" @click="go('/pages/admin/dashboard')">查看数据看板</view>
        </view>
      </view>

      <view class="uf-card uf-fade-up" style="margin-top: 20rpx;">
        <view class="uf-section-title">体测资源统计</view>
        <view class="admin-metric-grid">
          <view class="metric-item">
            <view class="metric-label">体测项目数</view>
            <view class="metric-value">{{ adminStats.itemCount }}</view>
          </view>
          <view class="metric-item">
            <view class="metric-label">标准条目数</view>
            <view class="metric-value">{{ adminStats.standardCount }}</view>
          </view>
        </view>
      </view>
    </template>

    <template v-else>
      <view class="uf-card uf-fade-up input-card">
        <view class="uf-section-title">录入体测成绩</view>

        <view class="item-grid">
          <view
            v-for="item in items"
            :key="item.code"
            :class="['item-chip', form.itemCode === item.code ? 'item-chip-active' : '']"
            @click="selectItem(item.code)"
          >
            <view class="chip-name">{{ item.name }}</view>
            <view class="chip-sub">{{ item.unit }} / {{ item.direction === 'lower' ? '越低越好' : '越高越好' }}</view>
          </view>
        </view>

        <input class="uf-input" v-model="form.scoreValue" type="digit" :placeholder="scorePlaceholder" />
        <button class="uf-btn-primary" @click="submit">提交成绩</button>
      </view>

      <view class="uf-card uf-fade-up" style="margin-top: 20rpx;">
        <view class="row-between">
          <view class="uf-section-title" style="margin: 0">{{ currentItemName }} 趋势</view>
          <text class="count">{{ chartHistory.length }} 条</text>
        </view>

        <view class="metric-grid" v-if="chartHistory.length">
          <view class="metric-item">
            <view class="metric-label">最新</view>
            <view class="metric-value">{{ metrics.latest }}</view>
          </view>
          <view class="metric-item">
            <view class="metric-label">最佳</view>
            <view class="metric-value">{{ metrics.best }}</view>
          </view>
          <view class="metric-item">
            <view class="metric-label">较首条</view>
            <view class="metric-value">{{ metrics.change }}</view>
          </view>
        </view>

        <canvas id="testTrendCanvas" canvas-id="testTrendCanvas" class="chart-canvas" style="width: 100%; height: 300rpx;"></canvas>
        <view v-if="!chartHistory.length" class="empty">当前项目暂无历史成绩。</view>
      </view>

      <view class="uf-card uf-fade-up" style="margin-top: 20rpx;">
        <view class="row-between">
          <view class="uf-section-title" style="margin: 0">薄弱项分析</view>
          <text class="count">覆盖项目 {{ weakness.totalItems }}</text>
        </view>
        <view v-if="weakness.weakItems.length">
          <view class="weak-row" v-for="(item, idx) in weakness.weakItems" :key="idx">
            <view>
              <view class="weak-title">{{ itemMap[item.itemCode] || item.itemCode }}</view>
              <view class="weak-sub">等级 {{ item.level }} / 标准分 {{ item.standardPoint }}</view>
            </view>
            <view class="weak-tip">{{ item.suggestion }}</view>
          </view>
        </view>
        <view v-else class="empty">暂无薄弱项，继续保持。</view>
      </view>

      <view class="uf-card uf-fade-up" style="margin-top: 20rpx;">
        <view class="row-between">
          <view class="uf-section-title" style="margin: 0">班级对比</view>
          <text class="count">{{ classCompare.className || '未设置班级' }}</text>
        </view>
        <view class="metric-grid">
          <view class="metric-item">
            <view class="metric-label">班级人数</view>
            <view class="metric-value">{{ classCompare.classSize || 0 }}</view>
          </view>
          <view class="metric-item">
            <view class="metric-label">我的排名</view>
            <view class="metric-value">{{ classCompare.myRank || 0 }}</view>
          </view>
          <view class="metric-item">
            <view class="metric-label">我的总分</view>
            <view class="metric-value">{{ classCompare.myTotalPoint || 0 }}</view>
          </view>
        </view>
        <view v-if="(classCompare.itemComparisons || []).length">
          <view class="weak-row" v-for="(row, idx) in classCompare.itemComparisons" :key="idx">
            <view>
              <view class="weak-title">{{ row.itemName || row.itemCode }}</view>
              <view class="weak-sub">我 {{ row.myPoint }} / 班均 {{ row.classAvgPoint }}</view>
            </view>
            <view class="weak-tip">差值 {{ row.gap }}</view>
          </view>
        </view>
        <view v-else class="empty">完善班级信息并录入成绩后可查看班级对比。</view>
      </view>

      <view class="uf-card uf-fade-up" style="margin-top: 20rpx;">
        <view class="row-between">
          <view class="uf-section-title" style="margin: 0">历史成绩</view>
          <text class="count">共 {{ history.length }} 条</text>
        </view>
        <view v-if="history.length">
          <view v-for="(item, idx) in history" :key="idx" class="score-row">
            <view>
              <view class="score-title">{{ itemMap[item.itemCode] || item.itemCode }}</view>
              <view class="score-sub">{{ formatDate(item.recordedDate) }}</view>
            </view>
            <view class="score-right">
              <view class="score-value">{{ item.scoreValue }}</view>
              <view :class="['uf-pill', levelClass(item.level)]">{{ item.level }} / {{ item.standardPoint }}</view>
            </view>
          </view>
        </view>
        <view v-else class="empty">还没有成绩记录，先录入第一条吧。</view>
      </view>
    </template>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin, setUser } from '@/common/auth'

const FALLBACK_ITEMS = [
  { code: 'pull_up', name: '引体向上', unit: '次', direction: 'higher' },
  { code: 'run_1000', name: '1000米跑', unit: '秒', direction: 'lower' },
  { code: 'run_800', name: '800米跑', unit: '秒', direction: 'lower' },
  { code: 'long_jump', name: '立定跳远', unit: 'cm', direction: 'higher' },
  { code: 'sit_up', name: '仰卧起坐', unit: '次', direction: 'higher' }
]

export default {
  data() {
    return {
      user: {},
      adminStats: {
        itemCount: 0,
        standardCount: 0
      },
      items: [],
      form: {
        itemCode: '',
        scoreValue: ''
      },
      history: [],
      chartHistory: [],
      weakness: {
        weakItems: [],
        totalItems: 0
      },
      classCompare: {
        className: '',
        classSize: 0,
        myRank: 0,
        myTotalPoint: 0,
        itemComparisons: []
      },
      metrics: {
        latest: '--',
        best: '--',
        change: '--'
      }
    }
  },
  computed: {
    isAdminRole() {
      return (this.user.userRole || 'student') === 'admin'
    },
    itemMap() {
      const map = {}
      this.items.forEach(item => {
        map[item.code] = item.name
      })
      return map
    },
    currentItem() {
      return this.items.find(item => item.code === this.form.itemCode) || null
    },
    currentItemName() {
      return this.currentItem ? this.currentItem.name : '体测成绩'
    },
    scorePlaceholder() {
      if (!this.currentItem) return '输入成绩值'
      return `输入成绩值（单位：${this.currentItem.unit}）`
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadCurrentUser()
    if (this.isAdminRole) {
      await this.loadAdminStats()
      return
    }
    await this.loadItems()
    await this.loadHistory()
    await this.loadWeakness()
    await this.loadClassCompare()
  },
  methods: {
    go(url) {
      uni.navigateTo({ url })
    },
    async loadCurrentUser() {
      const latestUser = await request({ url: '/user/get/login', showError: false }) || {}
      const localUser = uni.getStorageSync('user') || {}
      this.user = {
        ...localUser,
        ...latestUser,
        token: localUser.token
      }
      setUser(this.user)
    },
    async loadAdminStats() {
      const [itemRows, standardPage] = await Promise.all([
        request({ url: '/test/items', showError: false }),
        request({
          url: '/admin/standard/list/page',
          method: 'POST',
          data: { current: 1, pageSize: 1 },
          showError: false
        })
      ])
      this.adminStats.itemCount = (itemRows || []).length
      this.adminStats.standardCount = Number((standardPage && standardPage.total) || 0)
    },
    async loadWeakness() {
      this.weakness = await request({ url: '/test/analysis/weakness', showError: false }) || {
        weakItems: [],
        totalItems: 0
      }
    },
    async loadClassCompare() {
      this.classCompare = await request({ url: '/test/analysis/class/compare', showError: false }).catch(() => null) || {
        className: '',
        classSize: 0,
        myRank: 0,
        myTotalPoint: 0,
        itemComparisons: []
      }
    },
    async loadItems() {
      const data = await request({ url: '/test/items', showError: false })
      const mapped = (data || []).map(item => ({
        code: item.itemCode,
        name: item.itemName,
        unit: item.scoreUnit || '值',
        direction: item.scoreDirection || 'higher'
      }))
      this.items = mapped.length ? mapped : FALLBACK_ITEMS
      if (!this.form.itemCode && this.items.length) {
        this.form.itemCode = this.items[0].code
      }
    },
    selectItem(code) {
      this.form.itemCode = code
      this.loadChartHistory()
    },
    levelClass(level) {
      if (level === 'excellent') return 'level-excellent'
      if (level === 'good') return 'level-good'
      if (level === 'pass') return 'level-pass'
      return 'level-fail'
    },
    formatDate(v) {
      if (!v) return ''
      return String(v).slice(0, 10)
    },
    async submit() {
      if (!this.form.scoreValue) {
        uni.showToast({ title: '请输入成绩', icon: 'none' })
        return
      }
      await request({
        url: '/test/score/add',
        method: 'POST',
        data: {
          itemCode: this.form.itemCode,
          scoreValue: Number(this.form.scoreValue)
        }
      })
      uni.showToast({ title: '提交成功', icon: 'success' })
      this.form.scoreValue = ''
      await this.loadHistory()
      await this.loadWeakness()
      await this.loadClassCompare()
    },
    async loadHistory() {
      this.history = await request({ url: '/test/score/history' }) || []
      await this.loadChartHistory()
    },
    async loadChartHistory() {
      if (!this.form.itemCode) {
        this.chartHistory = []
        this.drawTrendChart()
        return
      }
      const data = await request({ url: `/test/score/history?itemCode=${this.form.itemCode}`, showError: false })
      this.chartHistory = (data || []).slice().reverse()
      this.computeMetrics()
      // 延迟绘制，确保 DOM 已经渲染
      setTimeout(() => {
        this.drawTrendChart()
      }, 100)
    },
    computeMetrics() {
      const values = this.chartHistory.map(i => Number(i.scoreValue)).filter(v => !Number.isNaN(v))
      if (!values.length) {
        this.metrics = { latest: '--', best: '--', change: '--' }
        return
      }
      const direction = this.currentItem && this.currentItem.direction === 'lower' ? 'lower' : 'higher'
      const latest = values[values.length - 1]
      const first = values[0]
      const best = direction === 'lower' ? Math.min(...values) : Math.max(...values)
      const delta = latest - first
      const signedDelta = delta > 0 ? `+${delta.toFixed(1)}` : delta.toFixed(1)
      this.metrics = {
        latest: latest.toFixed(1),
        best: best.toFixed(1),
        change: signedDelta
      }
    },
    drawTrendChart() {
      const ctx = uni.createCanvasContext('testTrendCanvas', this)
      // 获取实际的 Canvas 宽高（需要根据屏幕宽度计算）
      const screenWidth = uni.getSystemInfoSync().windowWidth
      const width = screenWidth - 64 // 减去左右 padding (32rpx = 16px)
      const height = 300
      const left = 50
      const right = width - 30
      const top = 20
      const bottom = height - 40

      ctx.clearRect(0, 0, width, height)

      ctx.setFillStyle('#f7fbff')
      ctx.fillRect(0, 0, width, height)

      // 绘制网格线
      ctx.setStrokeStyle('#dbe7f5')
      ctx.setLineWidth(1)
      for (let i = 0; i <= 4; i++) {
        const y = top + ((bottom - top) / 4) * i
        ctx.beginPath()
        ctx.moveTo(left, y)
        ctx.lineTo(right, y)
        ctx.stroke()
      }

      const values = this.chartHistory.map(i => Number(i.scoreValue)).filter(v => !Number.isNaN(v))
      if (!values.length) {
        ctx.draw()
        return
      }

      let min = Math.min(...values)
      let max = Math.max(...values)
      if (max === min) {
        max += 1
        min -= 1
      }

      const stepX = values.length > 1 ? (right - left) / (values.length - 1) : 0

      // 绘制折线
      ctx.setStrokeStyle('#4f6f8f')
      ctx.setLineWidth(2)
      ctx.beginPath()
      values.forEach((val, idx) => {
        const x = left + idx * stepX
        const y = bottom - ((val - min) / (max - min)) * (bottom - top)
        if (idx === 0) ctx.moveTo(x, y)
        else ctx.lineTo(x, y)
      })
      ctx.stroke()

      // 绘制数据点
      ctx.setFillStyle('#4f6f8f')
      values.forEach((val, idx) => {
        const x = left + idx * stepX
        const y = bottom - ((val - min) / (max - min)) * (bottom - top)
        ctx.beginPath()
        ctx.arc(x, y, 3, 0, Math.PI * 2)
        ctx.fill()
      })

      // 绘制 Y 轴标签
      ctx.setFillStyle('#6d86a8')
      ctx.setFontSize(12)
      ctx.fillText(String(max.toFixed(1)), 8, top + 8)
      ctx.fillText(String(min.toFixed(1)), 8, bottom + 4)

      // 绘制 X 轴标签
      const firstDate = this.chartHistory[0] ? this.formatDate(this.chartHistory[0].recordedDate) : ''
      const lastDate = this.chartHistory[this.chartHistory.length - 1]
        ? this.formatDate(this.chartHistory[this.chartHistory.length - 1].recordedDate)
        : ''
      ctx.setFillStyle('#7b90ad')
      ctx.setFontSize(11)
      if (firstDate) {
        ctx.fillText(firstDate, left, height - 8)
      }
      if (lastDate) {
        ctx.fillText(lastDate, right - 60, height - 8)
      }

      ctx.draw()
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.input-card {
  position: relative;
  overflow: hidden;
}

.input-card::after {
  content: '';
  position: absolute;
  right: -42rpx;
  top: -52rpx;
  width: 220rpx;
  height: 220rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(63, 102, 143, 0.08) 0%, rgba(63, 102, 143, 0) 68%);
}

.admin-desc {
  color: $text-secondary;
  font-size: 24rpx;
  line-height: 1.6;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.admin-tile {
  border-radius: $radius-md;
  padding: $spacing-md;
  background: $primary-light;
  border: 1px solid #d7e1ec;
  color: #334155;
  text-align: center;
  font-size: 24rpx;
}

.item-chip {
  border-radius: $radius-md;
  padding: $spacing-sm;
  background: $primary-light;
  border: 1px solid #d7e1ec;
  transition: all 0.3s ease;
}

.item-chip-active {
  background: #f2f6fb;
  border-color: $primary-color;
  box-shadow: 0 2rpx 8rpx rgba(63, 102, 143, 0.1);
}

.chip-name {
  color: $text-primary;
  font-size: 26rpx;
  font-weight: 600;
}

.chip-sub {
  margin-top: 8rpx;
  color: $text-secondary;
  font-size: 22rpx;
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.count {
  color: $text-secondary;
  font-size: 24rpx;
}

.admin-metric-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-sm;
  margin: $spacing-sm 0;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-sm;
  margin: $spacing-sm 0;
}

.metric-item {
  background: $primary-light;
  border: 1px solid #d7e1ec;
  border-radius: $radius-md;
  padding: $spacing-sm;
  text-align: center;
}

.metric-label {
  color: $text-secondary;
  font-size: 22rpx;
}

.metric-value {
  margin-top: 8rpx;
  color: $primary-color;
  font-size: 32rpx;
  font-weight: 700;
}

.chart-canvas {
  width: 100%;
  height: 300rpx;
  margin-top: $spacing-md;
  border-radius: $radius-md;
  background: $primary-light;
}

.weak-row {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
  padding: $spacing-sm 0;
  border-bottom: 1px dashed $border-color;
}

.weak-row:last-child {
  border-bottom: 0;
}

.weak-title {
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 500;
}

.weak-sub {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.weak-tip {
  width: 44%;
  font-size: 22rpx;
  color: $text-secondary;
  line-height: 1.5;
  text-align: right;
}

.score-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm 0;
  border-bottom: 1px dashed $border-color;
}

.score-row:last-child {
  border-bottom: 0;
}

.score-title {
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 500;
}

.score-sub {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.score-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: $spacing-xs;
}

.score-value {
  font-size: 32rpx;
  font-weight: 700;
  color: $primary-color;
}

.level-excellent {
  background: #eef3f8;
  color: #35526f;
}

.level-good {
  background: #edf2f8;
  color: #425f80;
}

.level-pass {
  background: #f7f3ea;
  color: #8a6a3f;
}

.level-fail {
  background: #f8eeee;
  color: #a35454;
}

.empty {
  color: $text-secondary;
  font-size: 26rpx;
  padding: $spacing-md 0;
}
</style>
