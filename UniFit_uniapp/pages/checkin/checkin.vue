<template>
  <view class="uf-page">
    <view class="hero uf-fade-up">
      <view>
        <view class="hero-title">{{ isAdminRole ? '训练打卡排行' : '每日训练打卡' }}</view>
        <view class="hero-sub">{{ isAdminRole ? '管理员可查看全站打卡排名数据' : '连续打卡越久，越容易稳定提升体测成绩' }}</view>
      </view>
      <view class="streak-ring">
        <view class="streak-num">{{ isAdminRole ? ranking.length : streak }}</view>
        <view class="streak-unit">{{ isAdminRole ? '人' : '天' }}</view>
      </view>
    </view>

    <view v-if="!isAdminRole" class="uf-card uf-fade-up" style="margin-top: 20rpx;">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0">今日打卡</view>
        <text class="hint">近30天</text>
      </view>
      <uni-calendar
        :insert="true"
        :selected="calendarCheckins"
        :showMonth="false"
      />
      <view class="checkin-tip">橙点表示已打卡，请输入本次实际运动时长后完成今日训练打卡。</view>
      <input v-if="planUnlocked && !todayCheckedIn" class="uf-input" v-model="duration" type="number" placeholder="请输入本次运动时长(分钟)" />
      <button v-if="planUnlocked && !todayCheckedIn" class="uf-btn-primary" @click="doCheckin">完成打卡</button>
      <view v-else-if="planUnlocked" class="locked-tip">今天已打卡，无需重复提交</view>
      <view v-else class="locked-tip">请先解锁训练计划后打卡</view>
    </view>

    <view class="uf-card uf-fade-up" style="margin-top: 20rpx;">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0">排行榜（近{{ rankingDays }}天）</view>
        <text class="hint">TOP {{ ranking.length }}</text>
      </view>
      <view v-if="ranking.length">
        <view class="rank-row" v-for="(item, idx) in ranking" :key="item.userId">
          <view class="rank-left">
            <view :class="['rank-no', idx < 3 ? 'rank-no-top' : '']">{{ idx + 1 }}</view>
            <view>
              <view class="name">{{ item.userName || ('用户' + item.userId) }}</view>
              <view class="meta">累计 {{ item.totalDuration }} 分钟</view>
            </view>
          </view>
          <view class="times">{{ item.checkinCount }} 次</view>
        </view>
      </view>
      <view v-else class="empty">还没有排行榜数据。</view>
    </view>

    <view class="uf-card uf-fade-up" style="margin-top: 20rpx;">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0">班级挑战赛（近{{ rankingDays }}天）</view>
        <text class="hint">TOP {{ classRanking.length }}</text>
      </view>
      <view v-if="!isAdminRole && myClassChallenge.className" class="meta" style="margin-bottom: 10rpx;">
        我的班级：{{ myClassChallenge.className }}，排名 {{ myClassChallenge.rank || 0 }}
      </view>
      <view v-if="classRanking.length">
        <view class="rank-row" v-for="(item, idx) in classRanking" :key="item.className + idx">
          <view class="rank-left">
            <view :class="['rank-no', idx < 3 ? 'rank-no-top' : '']">{{ idx + 1 }}</view>
            <view>
              <view class="name">{{ item.className || '未命名班级' }}</view>
              <view class="meta">参与 {{ item.participantCount || 0 }}/{{ item.memberCount || 0 }} 人 · {{ item.totalDuration || 0 }} 分钟</view>
            </view>
          </view>
          <view class="times">{{ item.checkinCount || 0 }} 次</view>
        </view>
      </view>
      <view v-else class="empty">暂无班级挑战数据。</view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin, setUser } from '@/common/auth'

export default {
  data() {
    return {
      user: {},
      planUnlocked: false,
      duration: '60',
      streak: 0,
      ranking: [],
      rankingDays: 7,
      classRanking: [],
      myClassChallenge: {},
      currentPlan: null,
      calendarCheckins: []
    }
  },
  computed: {
    isAdminRole() {
      return (this.user.userRole || 'student') === 'admin'
    },
    todayCheckedIn() {
      const today = new Date()
      const y = today.getFullYear()
      const m = `${today.getMonth() + 1}`.padStart(2, '0')
      const d = `${today.getDate()}`.padStart(2, '0')
      const todayText = `${y}-${m}-${d}`
      return (this.calendarCheckins || []).some(item => item && item.date === todayText)
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadCurrentUser()
    await this.refresh()
  },
  methods: {
    async loadCurrentUser() {
      const latestUser = await request({ url: '/user/get/login', showError: false }) || {}
      const localUser = uni.getStorageSync('user') || {}
      this.user = {
        ...localUser,
        ...latestUser,
        token: localUser.token
      }
      this.planUnlocked = Number(latestUser.planUnlocked) === 1
      setUser(this.user)
    },
    async refresh() {
      this.rankingDays = this.isAdminRole ? 30 : 7
      const rankingPromise = request({ url: `/checkin/ranking?days=${this.rankingDays}&topN=20`, showError: false })
      const classRankingPromise = request({ url: `/checkin/challenge/class?days=${this.rankingDays}&topN=20`, showError: false })
      if (this.isAdminRole) {
        this.ranking = await rankingPromise || []
        this.classRanking = await classRankingPromise || []
        this.myClassChallenge = {}
        this.streak = 0
        return
      }
      const [streak, ranking, classRanking, myClassChallenge, currentPlan, calendarCheckins] = await Promise.all([
        request({ url: '/checkin/streak', showError: false }),
        rankingPromise,
        classRankingPromise,
        request({ url: `/checkin/challenge/my-class?days=${this.rankingDays}`, showError: false }).catch(() => ({})),
        request({ url: '/plan/current', showError: false }).catch(() => null),
        request({ url: '/checkin/calendar?days=30', showError: false }).catch(() => [])
      ])
      this.streak = streak || 0
      this.ranking = ranking || []
      this.classRanking = classRanking || []
      this.myClassChallenge = myClassChallenge || {}
      this.currentPlan = currentPlan || null
      this.calendarCheckins = calendarCheckins || []
    },
    async doCheckin() {
      if (!this.currentPlan || !this.currentPlan.planId) {
        uni.showToast({ title: '请先生成训练计划', icon: 'none' })
        return
      }
      if (this.todayCheckedIn) {
        uni.showToast({ title: '今天已打卡', icon: 'none' })
        return
      }
      const duration = Number(String(this.duration || '').trim())
      if (!Number.isFinite(duration) || duration <= 0) {
        uni.showToast({ title: '请输入正确的运动时长', icon: 'none' })
        return
      }
      const result = await request({
        url: '/checkin/do',
        method: 'POST',
        data: {
          userPlanId: this.currentPlan.planId,
          durationMinutes: duration
        }
      })
      uni.showToast({ title: result && result.alreadyCheckedIn ? '今天已打过卡' : '打卡成功', icon: 'success' })
      this.refresh()
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.hero {
  border-radius: $radius-md;
  padding: 24rpx;
  color: #ffffff;
  background: #ffffff;
  border: 1rpx solid #e5e7eb;
  box-shadow: 0 2rpx 8rpx rgba(17, 24, 39, 0.04);
  margin-bottom: 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $text-primary;
}

.hero-sub {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $text-secondary;
  line-height: 1.5;
}

.streak-ring {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: $primary-light;
  border: 2rpx solid $primary-color;
}

.streak-num {
  font-size: 40rpx;
  font-weight: 700;
  color: $primary-color;
}

.streak-unit {
  font-size: 20rpx;
  margin-top: 4rpx;
  color: $primary-color;
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.hint {
  color: $text-secondary;
  font-size: 24rpx;
}

.checkin-tip {
  margin: 12rpx 0 16rpx;
  font-size: 22rpx;
  color: $text-secondary;
  line-height: 1.6;
}

.locked-tip {
  margin: 12rpx 0 16rpx;
  font-size: 24rpx;
  color: #9ca3af;
  text-align: center;
}

.rank-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md 0;
  border-bottom: 1px dashed $border-color;
}

.rank-row:last-child {
  border-bottom: 0;
}

.rank-left {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  flex: 1;
}

.rank-no {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: $primary-light;
  color: $primary-color;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 700;
}

.rank-no-top {
  background: #4f6f8f;
  color: #fff;
}

.name {
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 500;
}

.meta {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.times {
  font-size: 28rpx;
  font-weight: 700;
  color: $primary-color;
}

.empty {
  color: $text-secondary;
  font-size: 26rpx;
  padding: $spacing-lg 0;
  text-align: center;
}
</style>
