export type Purpose =
  | 'FRIENDSHIP'
  | 'DATING'
  | 'CAREER_GUIDANCE'
  | 'BUSINESS'
  | 'PROFESSIONAL_NETWORKING'
  | 'HOBBY_PARTNER'
  | 'MEETUP_PARTNER'

export type Profile = {
  id: number
  name: string | null
  bio: string | null
  city: string | null
  connectionPurposes: Purpose[]
  identityVerificationStatus: 'UNVERIFIED' | 'VERIFIED'
  emailVerified: boolean
}

export type ConnectionRequest = {
  id: number
  requesterId: number
  requesterName: string
  recipientId: number
  recipientName: string
  purpose: Purpose
  note: string | null
  status: string
  createdAt: string
}

export type MeetupRequest = {
  id: number
  meetupId: number
  meetupTitle: string
  requesterId: number
  requesterName: string
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED'
}

export type Connection = {
  id: number
  otherUserId: number
  otherUserName: string
  purposes: Purpose[]
  connectedAt: string
}

export type Message = {
  id: number
  senderId: number
  senderName: string
  body: string
  purpose: Purpose | null
  createdAt: string
}

export type Meetup = {
  id: number
  title: string
  description: string | null  date: string
  startTime: string
  endTime: string | null
  locationName: string
  latitude: number | null
  longitude: number | null
  maxParticipants: number
  category: string
  style: 'INFORMAL' | 'FORMAL'
  status?: string
  creator?: { id: number; name: string | null }
}

export type Notification = {
  id: number
  type: string
  title: string
  message: string
  actorId: number | null
  actorName: string | null
  referenceType: string | null
  referenceId: number | null
  createdAt: string
  readAt: string | null
}

const apiBase = import.meta.env.VITE_API_BASE_URL || '/api'
const backendOrigin = import.meta.env.VITE_BACKEND_ORIGIN || 'http://localhost:8080'

export function beginLinkedInLogin() {
  window.location.assign(`${backendOrigin}/oauth2/authorization/linkedin`)
}

export function storeLoginTokenFromCallback() {
  if (window.location.pathname !== '/auth/callback') return false
  const params = new URLSearchParams(window.location.hash.slice(1))
  const accessToken = params.get('access_token')
  if (accessToken) sessionStorage.setItem('find-someone-token', accessToken)
  window.history.replaceState({}, document.title, '/')
  return Boolean(accessToken)
}

export function clearToken() {
  sessionStorage.removeItem('find-someone-token')
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = sessionStorage.getItem('find-someone-token')
  const headers = new Headers(options.headers)
  if (token) headers.set('Authorization', `Bearer ${token}`)
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  const response = await fetch(`${apiBase}${path}`, { ...options, headers })
  if (!response.ok) {
    if (response.status === 401) clearToken()
    const message = await response.text()
    throw new Error(message || `Request failed (${response.status})`)
  }
  if (response.status === 204) return undefined as T  return response.json() as Promise<T>
}

const json = (body: unknown): RequestInit => ({ method: 'POST', body: JSON.stringify(body) })

export const api = {
  me: () => request<Profile>('/users/me'),
  updateProfile: (profile: Pick<Profile, 'name' | 'bio' | 'city' | 'connectionPurposes'>) =>
    request<Profile>('/users/me', { method: 'PATCH', body: JSON.stringify(profile) }),
  profiles: (purpose: string, query: string, city: string) => {
    const params = new URLSearchParams()
    if (purpose) params.set('purpose', purpose)
    if (query) params.set('query', query)
    if (city) params.set('city', city)
    const search = params.toString()
    return request<Profile[]>(`/users${search ? `?${search}` : ''}`)
  },
  sendConnectionRequest: (recipientId: number, purpose: Purpose, note: string) =>
    request<ConnectionRequest>('/connections/requests', json({ recipientId, purpose, note })),
  incomingRequests: () => request<ConnectionRequest[]>('/connections/requests/incoming'),
  outgoingRequests: () => request<ConnectionRequest[]>('/connections/requests/outgoing'),
  respondToRequest: (requestId: number, action: 'accept' | 'decline') =>
    request<ConnectionRequest>(`/connections/requests/${requestId}/${action}`, { method: 'PATCH' }),
  cancelConnectionRequest: (requestId: number) =>
    request<void>(`/connections/requests/${requestId}`, { method: 'DELETE' }),
  connections: () => request<Connection[]>('/connections'),
  messages: (connectionId: number, purpose: Purpose) =>
    request<Message[]>(`/connections/${connectionId}/messages?purpose=${purpose}&page=0&size=50`),
  sendMessage: (connectionId: number, purpose: Purpose, body: string) =>
    request<Message>(`/connections/${connectionId}/messages`, json({ purpose, body })),
  reportMessage: (messageId: number, reason: string) =>
    request('/safety/messages/' + messageId + '/reports', json({ reason })),
  block: (userId: number) => request(`/safety/blocks/${userId}`, json({})),
  meetups: () => request<Meetup[]>('/meetups'),
  createMeetup: (meetup: Omit<Meetup, 'id' | 'creator'>) => request<Meetup>('/meetups', json(meetup)),
  requestMeetup: (meetupId: number) => request(`/meetups/${meetupId}/requests`, json({})),
  myMeetupRequests: () => request<MeetupRequest[]>('/meetup-requests'),
  meetupRequests: (meetupId: number) => request<MeetupRequest[]>(`/meetups/${meetupId}/requests`),
  respondToMeetupRequest: (requestId: number, action: 'accept' | 'reject') =>
    request<MeetupRequest>(`/meetup-requests/${requestId}/${action}`, { method: 'PATCH' }),
  cancelMeetupRequest: (requestId: number) =>
    request<void>(`/meetup-requests/${requestId}`, { method: 'DELETE' }),
  notifications: () => request<Notification[]>('/notifications?page=0&size=20'),
  unreadCount: () => request<number>('/notifications/unread-count'),
  markRead: (notificationId: number) => request(`/notifications/${notificationId}/read`, { method: 'PATCH' }),
  markAllRead: () => request<void>('/notifications/read-all', { method: 'PATCH' }),
}
