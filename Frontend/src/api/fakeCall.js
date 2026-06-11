import api from './axios.js'
export const getTemplates       = ()          => api.get('/fake-calls/templates')
export const createTemplate     = (data)      => api.post('/fake-calls/templates', data)
export const updateTemplate     = (id, data)  => api.put(`/fake-calls/templates/${id}`, data)
export const deleteTemplate     = (id)        => api.delete(`/fake-calls/templates/${id}`)
export const setDefaultTemplate = (id)        => api.patch(`/fake-calls/templates/${id}/default`)
export const triggerFakeCall    = (data)      => api.post('/fake-calls/trigger', data)
