import api from './axios.js'
export const startTimer    = (data) => api.post('/timer/start', data)
export const checkIn       = (id)   => api.patch(`/timer/${id}/checkin`)
export const cancelTimer   = (id)   => api.patch(`/timer/${id}/cancel`)
export const getActiveTimer= ()     => api.get('/timer/active')
export const getHistory    = ()     => api.get('/timer/history')
