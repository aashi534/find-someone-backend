import { type FormEvent, type ReactNode, useCallback, useEffect, useMemo, useState } from 'react'
import { api, beginLinkedInLogin, clearToken, storeLoginTokenFromCallback } from './api'
import type { Connection, ConnectionRequest, Meetup, MeetupRequest, Message, Notification, Profile, Purpose } from './api'

type Tab = 'discover' | 'requests' | 'connections' | 'meetups'

const purposes: Purpose[] = ['FRIENDSHIP', 'DATING', 'CAREER_GUIDANCE', 'BUSINESS', 'PROFESSIONAL_NETWORKING', 'HOBBY_PARTNER', 'MEETUP_PARTNER']
const labels: Record<string, string> = {
  FRIENDSHIP: 'Friendship', DATING: 'Dating', CAREER_GUIDANCE: 'Career guidance', BUSINESS: 'Business',
  PROFESSIONAL_NETWORKING: 'Professional networking', HOBBY_PARTNER: 'Hobby partner', MEETUP_PARTNER: 'Meetup partner',
  SPORTS: 'Sports', TECHNOLOGY: 'Technology', CAREER: 'Career', HEALTH_WELLNESS: 'Health & wellness', SOCIAL: 'Social', ARTS: 'Arts', OTHER: 'Other',
}

function App() {
  const [authenticated, setAuthenticated] = useState(() => Boolean(sessionStorage.getItem('find-someone-token')) || storeLoginTokenFromCallback())
  const [profile, setProfile] = useState<Profile | null>(null)
  const [profiles, setProfiles] = useState<Profile[]>([])
  const [requests, setRequests] = useState<ConnectionRequest[]>([])
  const [sentRequests, setSentRequests] = useState<ConnectionRequest[]>([])
  const [connections, setConnections] = useState<Connection[]>([])
  const [meetups, setMeetups] = useState<Meetup[]>([])
  const [myMeetupRequests, setMyMeetupRequests] = useState<MeetupRequest[]>([])
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [unread, setUnread] = useState(0)
  const [tab, setTab] = useState<Tab>('discover')
  const [purposeFilter, setPurposeFilter] = useState('')
  const [query, setQuery] = useState('')
  const [requestPurpose, setRequestPurpose] = useState<Record<number, Purpose>>({})
  const [activeChat, setActiveChat] = useState<Connection | null>(null)
  const [chatPurpose, setChatPurpose] = useState<Purpose | ''>('')
  const [messages, setMessages] = useState<Message[]>([])
  const [noticeOpen, setNoticeOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [createOpen, setCreateOpen] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const loadMain = useCallback(async () => {
    setBusy(true)
    setError('')
    try {
      const [me, discoverable, incoming, outgoing, connected, events, myRsvps, inbox, count] = await Promise.all([
        api.me(), api.profiles(purposeFilter, query), api.incomingRequests(), api.outgoingRequests(), api.connections(),
        api.meetups(), api.myMeetupRequests(), api.notifications(), api.unreadCount(),
      ])
      setProfile(me); setProfiles(discoverable); setRequests(incoming); setSentRequests(outgoing); setConnections(connected)
      setMeetups(events.filter(event => event.status !== 'CANCELLED'))
      setMyMeetupRequests(myRsvps)
      setNotifications(inbox); setUnread(count)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Could not load your account.')
      if (!sessionStorage.getItem('find-someone-token')) setAuthenticated(false)
    } finally { setBusy(false) }
  }, [purposeFilter, query])

  useEffect(() => { if (authenticated) void loadMain() }, [authenticated, loadMain])

  const purposeOptions = useMemo(() => purposes, [])

  async function act(task: () => Promise<unknown>, success?: string) {
    setError('')
    try { await task(); await loadMain(); if (success) setError(success) }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'That action could not be completed.') }
  }

  async function openChat(connection: Connection) {
    setActiveChat(connection)
    const purpose = connection.purposes[0] ?? ''
    setChatPurpose(purpose)
    if (!purpose) { setMessages([]); return }
    try { setMessages(await api.messages(connection.id, purpose)) }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Could not load chat.') }
  }

  async function changeChatPurpose(purpose: Purpose) {
    setChatPurpose(purpose)
    if (!activeChat) return
    try { setMessages(await api.messages(activeChat.id, purpose)) }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Could not load chat.') }
  }

  async function signOut() {
    clearToken(); setAuthenticated(false); setProfile(null); setActiveChat(null)
  }

  if (!authenticated) {
    return <Login error={error} onSignIn={beginLinkedInLogin} />
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#home" onClick={event => event.preventDefault()}><span className="brand-mark">f.</span><span>find someone</span></a>
        <div className="sidebar-caption">YOUR SPACE</div>
        <nav className="side-nav" aria-label="Main navigation">
          <NavButton active={tab === 'discover'} icon="search" label="Discover" onClick={() => { setTab('discover'); setActiveChat(null) }} />
          <NavButton active={tab === 'requests'} icon="request" label="Requests" count={requests.length} onClick={() => { setTab('requests'); setActiveChat(null) }} />
          <NavButton active={tab === 'connections'} icon="people" label="Connections" onClick={() => { setTab('connections'); setActiveChat(null) }} />
          <NavButton active={tab === 'meetups'} icon="calendar" label="Meetups" onClick={() => { setTab('meetups'); setActiveChat(null) }} />
        </nav>
        <div className="sidebar-note"><span className="note-spark">✳</span><p>Good connections start with a shared reason to meet.</p></div>
        <button className="user-mini" onClick={signOut} title="Sign out"><Avatar name={profile?.name ?? 'You'} /><span className="mini-text"><b>{profile?.name || 'Your profile'}</b><small>Signed in with LinkedIn</small></span><span className="signout">↗</span></button>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="mobile-brand"><span className="brand-mark">f.</span> find someone</div>
          <div className="topbar-greeting"><span className="online-dot" /> A more human way to connect</div>
          <button className="edit-profile-link" onClick={() => setProfileOpen(true)}>Edit profile <span>↗</span></button>
          <button className={`icon-button ${noticeOpen ? 'selected' : ''}`} aria-label="Notifications" onClick={() => setNoticeOpen(!noticeOpen)}>
            <Icon name="bell" />{unread > 0 && <span className="notification-badge">{unread}</span>}
          </button>
        </header>

        {error && <div className={error.startsWith('Done:') ? 'toast success' : 'toast'} role="status"><span>{error.startsWith('Done:') ? '✓' : '!'}</span>{error}<button onClick={() => setError('')} aria-label="Dismiss">×</button></div>}

        <div className="content-wrap">
          {tab === 'discover' && <DiscoverView profiles={profiles} purposeOptions={purposeOptions} purposeFilter={purposeFilter} query={query} busy={busy} ownId={profile?.id ?? -1} requestPurpose={requestPurpose} onPurposeFilter={setPurposeFilter} onQuery={setQuery} onRequestPurpose={(id, purpose) => setRequestPurpose(old => ({ ...old, [id]: purpose }))} onConnect={(person, purpose) => void act(() => api.sendConnectionRequest(person.id, purpose, ''), 'Done: connection request sent.')} onBlock={id => void act(() => api.block(id), 'Done: member blocked.')} />}
          {tab === 'requests' && <RequestsView requests={requests} sentRequests={sentRequests} busy={busy} onRespond={(id, action) => void act(() => api.respondToRequest(id, action), `Done: request ${action}ed.`)} onCancel={id => void act(() => api.cancelConnectionRequest(id), 'Done: request cancelled.')} />}
          {tab === 'connections' && (activeChat
            ? <ChatView connection={activeChat} purpose={chatPurpose} purposes={purposeOptions.filter(p => activeChat.purposes.includes(p))} messages={messages} ownId={profile?.id ?? -1} onBack={() => setActiveChat(null)} onPurpose={changeChatPurpose} onSend={async body => { if (!chatPurpose) return; await api.sendMessage(activeChat.id, chatPurpose, body); setMessages(await api.messages(activeChat.id, chatPurpose)) }} onReport={message => { const reason = window.prompt('Why are you reporting this message? (HARASSMENT, SCAM_OR_SPAM, INAPPROPRIATE_CONTENT, OTHER)', 'INAPPROPRIATE_CONTENT'); if (reason) void act(() => api.reportMessage(message.id, reason.trim().toUpperCase()), 'Done: message report submitted.') }} />
            : <ConnectionsView connections={connections} busy={busy} onChat={openChat} />)}
          {tab === 'meetups' && <MeetupsView meetups={meetups} myRequests={myMeetupRequests} ownId={profile?.id ?? -1} busy={busy} onCreate={() => setCreateOpen(true)} onJoin={id => act(() => api.requestMeetup(id), 'Done: meetup request sent.')} onCancelRsvp={id => act(() => api.cancelMeetupRequest(id), 'Done: RSVP updated.')} onManageRsvp={(id, action) => act(() => api.respondToMeetupRequest(id, action), `Done: RSVP ${action}ed.`)} />}
        </div>
      </main>

      {noticeOpen && <NotificationsPanel notifications={notifications} unread={unread} onClose={() => setNoticeOpen(false)} onRead={id => void act(() => api.markRead(id))} onReadAll={() => void act(() => api.markAllRead())} />}
      {profileOpen && profile && <ProfileModal profile={profile} onClose={() => setProfileOpen(false)} onSave={async updated => { try { await api.updateProfile(updated); setProfileOpen(false); await loadMain() } catch (cause) { setError(cause instanceof Error ? cause.message : 'Could not save your profile.') } }} />}
      {createOpen && <CreateMeetupModal onClose={() => setCreateOpen(false)} onCreate={async event => { try { await api.createMeetup(event); setCreateOpen(false); await loadMain() } catch (cause) { setError(cause instanceof Error ? cause.message : 'Could not publish the meetup.') } }} />}
      <nav className="mobile-nav" aria-label="Mobile navigation">
        <NavButton active={tab === 'discover'} icon="search" label="Explore" onClick={() => { setTab('discover'); setActiveChat(null) }} />
        <NavButton active={tab === 'requests'} icon="request" label="Requests" count={requests.length} onClick={() => { setTab('requests'); setActiveChat(null) }} />
        <NavButton active={tab === 'connections'} icon="people" label="Chats" onClick={() => { setTab('connections'); setActiveChat(null) }} />
        <NavButton active={tab === 'meetups'} icon="calendar" label="Meetups" onClick={() => { setTab('meetups'); setActiveChat(null) }} />
      </nav>
    </div>
  )
}

function Login({ onSignIn, error }: { onSignIn: () => void; error: string }) {
  return <main className="login-page"><div className="login-card"><a className="brand login-brand"><span className="brand-mark">f.</span><span>find someone</span></a><div className="login-art"><div className="orbit orbit-one"/><div className="orbit orbit-two"/><span className="art-star">✳</span><span className="art-person person-one">A</span><span className="art-person person-two">M</span><span className="art-person person-three">R</span><span className="art-heart">↗</span></div><p className="eyebrow">MEET WITH MEANING</p><h1>Your people are<br/>closer than you think.</h1><p className="login-copy">Find a career sounding board, a badminton partner, or someone new to explore the city with. Start with a real profile and a reason to connect.</p><button className="linkedin-button" onClick={onSignIn}><span className="linkedin-icon">in</span> Continue with LinkedIn <span>→</span></button>{error && <p className="login-error">{error}</p>}<p className="login-footnote">One profile. Many ways to connect.<br/>Your conversations stay yours.</p></div><div className="login-side-copy"><span className="side-quote">“</span><p>Not another feed.<br/>A place to find<br/><i>your kind of people.</i></p><div className="quote-line"/><small>PROFESSIONAL · PERSONAL · LOCAL</small></div></main>
}

function NavButton({ active, icon, label, count, onClick }: { active: boolean; icon: 'search' | 'request' | 'people' | 'calendar'; label: string; count?: number; onClick: () => void }) {
  return <button className={`nav-item ${active ? 'active' : ''}`} onClick={onClick}><span className="nav-icon"><Icon name={icon}/></span><span>{label}</span>{count ? <small>{count}</small> : null}</button>
}

function Icon({ name }: { name: 'search' | 'request' | 'people' | 'calendar' | 'bell' }) {
  const paths: Record<typeof name, ReactNode> = {
    search: <><circle cx="10.8" cy="10.8" r="6.2"/><path d="m15.4 15.4 4.1 4.1"/></>,
    request: <><path d="M5 12h13"/><path d="m13 6 6 6-6 6"/><path d="M5 5v14"/></>,
    people: <><path d="M16 20v-1.7a3.3 3.3 0 0 0-3.3-3.3H7.3A3.3 3.3 0 0 0 4 18.3V20"/><circle cx="10" cy="7.5" r="3.5"/><path d="M16.2 4.3a3.5 3.5 0 0 1 0 6.8M17 15h.7a3.3 3.3 0 0 1 3.3 3.3V20"/></>,
    calendar: <><rect x="3.5" y="5" width="17" height="16" rx="2.5"/><path d="M7.5 3v4M16.5 3v4M3.5 10h17"/><path d="m8 14 2 2 4-4"/></>,
    bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></>,
  }
  return <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">{paths[name]}</svg>
}

function DiscoverView({ profiles, purposeOptions, purposeFilter, query, busy, ownId, requestPurpose, onPurposeFilter, onQuery, onRequestPurpose, onConnect, onBlock }: {
  profiles: Profile[]; purposeOptions: Purpose[]; purposeFilter: string; query: string; busy: boolean; ownId: number; requestPurpose: Record<number, Purpose>;
  onPurposeFilter: (value: string) => void; onQuery: (value: string) => void; onRequestPurpose: (id: number, purpose: Purpose) => void;
  onConnect: (profile: Profile, purpose: Purpose) => void; onBlock: (id: number) => void;
}) {
  const [searchText, setSearchText] = useState(query)
  useEffect(() => { const timeout = window.setTimeout(() => onQuery(searchText), 250); return () => window.clearTimeout(timeout) }, [searchText, onQuery])
  return <>
    <section className="hero-panel"><div className="hero-content"><p className="eyebrow">A GOOD DAY TO MEET SOMEONE</p><h1>Find a connection<br/><em>that feels like you.</em></h1><p>People with shared intentions, interests, and room in their day.</p><button className="dark-button" onClick={() => document.getElementById('people-grid')?.scrollIntoView({ behavior: 'smooth' })}>Explore people <span>↓</span></button></div><div className="hero-illustration"><div className="hero-sun"/><div className="hero-loop loop-a"/><div className="hero-loop loop-b"/><div className="hero-disc disc-a">N</div><div className="hero-disc disc-b">J</div><div className="hero-disc disc-c">S</div><span className="hero-spark">✳</span><span className="hero-tag">same city,<br/>new stories</span></div><span className="hero-side-label">MEET SOMEONE NEW — TODAY</span></section>
    <div className="section-heading" id="people-grid"><div><p className="eyebrow">YOUR NEXT HELLO</p><h2>People to meet <span className="heading-count">{profiles.length.toString().padStart(2, '0')}</span></h2></div><button className="filter-button" onClick={() => { onPurposeFilter(''); setSearchText(''); onQuery('') }}>Clear filters <span>↗</span></button></div>
    <div className="filter-row"><label className="search-box"><span><Icon name="search"/></span><input aria-label="Search profiles" placeholder="Search names or interests" value={searchText} onChange={event => setSearchText(event.target.value)}/><kbd>⌘ K</kbd></label><div className="purpose-chips"><button className={`filter-chip ${!purposeFilter ? 'selected' : ''}`} onClick={() => onPurposeFilter('')}>All people</button>{purposeOptions.slice(0, 5).map(p => <button key={p} className={`filter-chip ${purposeFilter === p ? 'selected' : ''}`} onClick={() => onPurposeFilter(purposeFilter === p ? '' : p)}>{labels[p]}</button>)}</div></div>
    {busy && profiles.length === 0 ? <div className="loading-box">Finding people for you…</div> : profiles.length === 0 ? <div className="empty-state"><span>✳</span><h3>No one here just yet</h3><p>Try a different purpose or search term.</p></div> : <div className="people-grid">{profiles.filter(person => person.id !== ownId).map((person, index) => {
      const choices = person.connectionPurposes || []
      const selected = requestPurpose[person.id] ?? choices[0]
      return <article className="person-card" key={person.id}><div className={`person-cover cover-${index % 4}`}><span className="cover-stamp">{index % 2 ? 'OPEN TO A HELLO' : 'NEW CONNECTION'}</span><Avatar name={person.name ?? 'Someone'} size="large"/><button className="more-button" aria-label="Block member" title="Block this member" onClick={() => window.confirm(`Block ${person.name || 'this member'}?`) && onBlock(person.id)}>···</button></div><div className="person-info"><div className="person-name-row"><h3>{person.name || 'New member'}</h3><span className="verified-dot" title={person.identityVerificationStatus === 'VERIFIED' ? 'Identity verified' : 'LinkedIn sign-in only; identity is not verified'}>{person.identityVerificationStatus === 'VERIFIED' ? '✓' : 'in'}</span></div><p className="person-bio">{person.bio || 'Here to meet people with shared interests and good energy.'}</p><div className="purpose-list">{choices.slice(0, 3).map(p => <span className="purpose-tag" key={p}>{labels[p] ?? p}</span>)}</div><div className="person-actions"><select aria-label={`Choose purpose to connect with ${person.name}`} value={selected ?? ''} onChange={event => onRequestPurpose(person.id, event.target.value as Purpose)}>{choices.map(p => <option key={p} value={p}>{labels[p] ?? p}</option>)}</select><button className="connect-button" disabled={!selected} onClick={() => selected && onConnect(person, selected)}>Connect <span>↗</span></button></div></div></article>
    })}</div>}
    <div className="soft-footer"><span>✳</span><p>A small hello can change the shape of your week.</p><span className="footer-rule"/><small>FIND SOMEONE · BENGALURU</small></div>
  </>
}

function RequestsView({ requests, sentRequests, busy, onRespond, onCancel }: { requests: ConnectionRequest[]; sentRequests: ConnectionRequest[]; busy: boolean; onRespond: (id: number, action: 'accept' | 'decline') => void; onCancel: (id: number) => void }) {
  const [view, setView] = useState<'incoming' | 'sent'>('incoming')
  const shown = view === 'incoming' ? requests : sentRequests
  return <><PageTitle eyebrow="OPEN DOORS" title="Connection requests" copy="Every good conversation starts with a small invitation." /><div className="meetup-filters"><button className={`filter-chip ${view === 'incoming' ? 'selected' : ''}`} onClick={() => setView('incoming')}>Received · {requests.length}</button><button className={`filter-chip ${view === 'sent' ? 'selected' : ''}`} onClick={() => setView('sent')}>Sent · {sentRequests.length}</button></div>{busy && shown.length === 0 ? <div className="loading-box">Loading requests…</div> : shown.length === 0 ? <Empty title={view === 'incoming' ? "You're all caught up" : 'No sent requests'} copy={view === 'incoming' ? 'New connection requests will show up here.' : 'Requests you send will appear here until they are answered.'}/> : <div className="request-list">{shown.map(request => <article className="request-card" key={request.id}><Avatar name={view === 'incoming' ? request.requesterName : request.recipientName}/><div className="request-copy"><h3>{view === 'incoming' ? request.requesterName : request.recipientName}</h3><p>{view === 'incoming' ? 'Wants to connect with you' : 'You asked to connect'} for <b>{labels[request.purpose]}</b></p>{request.note && <blockquote>“{request.note}”</blockquote>}<small>{view === 'incoming' ? `Received ${new Date(request.createdAt).toLocaleDateString()}` : `Sent ${new Date(request.createdAt).toLocaleDateString()} · ${request.status.toLowerCase()}`}</small></div>{view === 'incoming' ? <div className="request-actions"><button className="dark-button small" onClick={() => onRespond(request.id, 'accept')}>Accept <span>✓</span></button><button className="text-button" onClick={() => onRespond(request.id, 'decline')}>Decline</button></div> : request.status === 'PENDING' && <div className="request-actions"><button className="text-button" onClick={() => onCancel(request.id)}>Cancel request</button></div>}</article>)}</div>}</>
}

function ConnectionsView({ connections, busy, onChat }: { connections: Connection[]; busy: boolean; onChat: (connection: Connection) => void }) {
  return <><PageTitle eyebrow="YOUR CIRCLE" title="Connections" copy="The people you’ve chosen to let into your world." />{busy && connections.length === 0 ? <div className="loading-box">Loading connections…</div> : connections.length === 0 ? <Empty title="Your circle starts here" copy="Explore profiles and send a connection request to get started."/> : <div className="connection-grid">{connections.map(connection => <article className="connection-card" key={connection.id}><Avatar name={connection.otherUserName} size="large"/><h3>{connection.otherUserName}</h3><p>Connected {new Date(connection.connectedAt).toLocaleDateString(undefined, { month: 'short', year: 'numeric' })}</p><div className="purpose-list centered">{connection.purposes.map(p => <span className="purpose-tag" key={p}>{labels[p]}</span>)}</div><button className="dark-button full" onClick={() => onChat(connection)}>Open conversation <span>↗</span></button></article>)}</div>}</>
}

function ChatView({ connection, purpose, purposes, messages, ownId, onBack, onPurpose, onSend, onReport }: { connection: Connection; purpose: Purpose | ''; purposes: Purpose[]; messages: Message[]; ownId: number; onBack: () => void; onPurpose: (purpose: Purpose) => void; onSend: (body: string) => Promise<void>; onReport: (message: Message) => void }) {
  const [body, setBody] = useState('')
  const [sending, setSending] = useState(false)
  async function submit(event: FormEvent) { event.preventDefault(); if (!body.trim() || !purpose) return; setSending(true); try { await onSend(body.trim()); setBody('') } finally { setSending(false) } }
  return <section className="chat-shell"><div className="chat-header"><button className="back-button" onClick={onBack}>←</button><Avatar name={connection.otherUserName}/><div><b>{connection.otherUserName}</b><small>Connected · your conversations stay private</small></div><label className="chat-purpose">Talking about<select value={purpose} onChange={event => onPurpose(event.target.value as Purpose)}>{purposes.map(p => <option key={p} value={p}>{labels[p]}</option>)}</select></label></div><div className="chat-body">{messages.length === 0 ? <div className="chat-empty"><span>✳</span><h3>Start with a hello</h3><p>Keep it kind, curious, and true to why you connected.</p></div> : messages.slice().reverse().map(message => <div key={message.id} className={`message-row ${message.senderId === ownId ? 'mine' : ''}`}><div className="message-bubble"><small>{message.senderId === ownId ? 'You' : message.senderName} · {new Date(message.createdAt).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}</small><p>{message.body}</p>{message.senderId !== ownId && <button onClick={() => onReport(message)}>Report message</button>}</div></div>)}</div><form className="chat-compose" onSubmit={submit}><input value={body} maxLength={5000} onChange={event => setBody(event.target.value)} placeholder="Write something thoughtful…"/><button type="submit" disabled={sending || !purpose || !body.trim()}>{sending ? 'Sending…' : 'Send'} <span>↑</span></button></form></section>
}

function MeetupsView({ meetups, myRequests, ownId, busy, onCreate, onJoin, onCancelRsvp, onManageRsvp }: { meetups: Meetup[]; myRequests: MeetupRequest[]; ownId: number; busy: boolean; onCreate: () => void; onJoin: (id: number) => Promise<void>; onCancelRsvp: (id: number) => Promise<void>; onManageRsvp: (id: number, action: 'accept' | 'reject') => Promise<void> }) {
  const [style, setStyle] = useState<'ALL' | 'FORMAL' | 'INFORMAL'>('ALL')
  const [view, setView] = useState<'events' | 'mine'>('events')
  const [managedMeetup, setManagedMeetup] = useState<number | null>(null)
  const [organizerRequests, setOrganizerRequests] = useState<MeetupRequest[]>([])
  const [requestBusy, setRequestBusy] = useState(false)
  const filtered = meetups.filter(meetup => style === 'ALL' || meetup.style === style)
  async function openOrganizerRequests(meetupId: number) {
    setManagedMeetup(meetupId); setRequestBusy(true)
    try { setOrganizerRequests(await api.meetupRequests(meetupId)) }
    catch { setOrganizerRequests([]) }
    finally { setRequestBusy(false) }
  }
  const myRsvpIds = new Set(myRequests.filter(request => request.status === 'PENDING' || request.status === 'ACCEPTED').map(request => request.meetupId))
  const activeRsvps = myRequests.filter(request => request.status === 'PENDING' || request.status === 'ACCEPTED')
  return <><div className="meetup-title-row"><PageTitle eyebrow="OUT IN THE WORLD" title="Meetups" copy="From a casual game to a workshop worth showing up for."/><button className="dark-button create-button" onClick={onCreate}>Host a meetup <span>＋</span></button></div><div className="meetup-filters"><button className={`filter-chip ${view === 'events' ? 'selected' : ''}`} onClick={() => setView('events')}>Explore meetups</button><button className={`filter-chip ${view === 'mine' ? 'selected' : ''}`} onClick={() => setView('mine')}>My RSVPs · {activeRsvps.length}</button></div>{view === 'events' ? <><div className="meetup-filters">{(['ALL', 'INFORMAL', 'FORMAL'] as const).map(value => <button key={value} className={`filter-chip ${style === value ? 'selected' : ''}`} onClick={() => setStyle(value)}>{value === 'ALL' ? 'All meetups' : value === 'FORMAL' ? 'Workshops & events' : 'Casual plans'}</button>)}</div>{busy && meetups.length === 0 ? <div className="loading-box">Finding meetups…</div> : filtered.length === 0 ? <Empty title="No meetups yet" copy="Be the first to put a plan on the calendar."/> : <div className="meetup-grid">{filtered.map((event, i) => <article className="meetup-card" key={event.id}><div className={`meetup-art meetup-art-${i % 3}`}><span className="meetup-kind">{event.style === 'FORMAL' ? '✦ FORMAL' : '☼ INFORMAL'}</span><span className="meetup-category">{labels[event.category] ?? event.category}</span><span className="meetup-day">{new Date(`${event.date}T00:00:00`).toLocaleDateString(undefined, { day: '2-digit' })}</span><span className="meetup-month">{new Date(`${event.date}T00:00:00`).toLocaleDateString(undefined, { month: 'short' }).toUpperCase()}</span></div><div className="meetup-details"><h3>{event.title}</h3><p>{event.description || 'Come meet people and share a good experience.'}</p><div className="meetup-meta"><span>◷ {event.startTime?.slice(0, 5)}</span><span>⌖ {event.locationName}</span></div>{event.creator?.id === ownId ? <button className="outline-button" onClick={() => void openOrganizerRequests(event.id)}>Manage RSVPs <span>↗</span></button> : <button className="outline-button" disabled={myRsvpIds.has(event.id)} onClick={() => void onJoin(event.id)}>{myRsvpIds.has(event.id) ? 'Request sent' : 'Ask to join'} <span>↗</span></button>}</div>{managedMeetup === event.id && <div className="meetup-rsvp-panel"><div className="rsvp-panel-heading"><b>Join requests</b><button onClick={() => setManagedMeetup(null)}>Close</button></div>{requestBusy ? <small>Loading requests…</small> : organizerRequests.length === 0 ? <small>No requests for this meetup yet.</small> : organizerRequests.filter(request => request.status === 'PENDING').map(request => <div className="rsvp-row" key={request.id}><span>{request.requesterName}</span><div><button onClick={async () => { await onManageRsvp(request.id, 'accept'); await openOrganizerRequests(event.id) }}>Accept</button><button onClick={async () => { await onManageRsvp(request.id, 'reject'); await openOrganizerRequests(event.id) }}>Decline</button></div></div>)}</div>}</article>)}</div>}</> : activeRsvps.length === 0 ? <Empty title="No meetup requests yet" copy="Ask to join a meetup and track the RSVP here."/> : <div className="request-list">{activeRsvps.map(request => <article className="request-card" key={request.id}><span className="rsvp-date">{labels[request.status] ?? request.status}</span><div className="request-copy"><h3>{request.meetupTitle}</h3><p>{request.status === 'ACCEPTED' ? 'You’re on the attendee list.' : 'Waiting for the organizer to respond.'}</p></div><div className="request-actions"><button className="text-button" onClick={() => void onCancelRsvp(request.id)}>{request.status === 'ACCEPTED' ? 'Leave meetup' : 'Withdraw request'}</button></div></article>)}</div>}</>
}

function CreateMeetupModal({ onClose, onCreate }: { onClose: () => void; onCreate: (event: Omit<Meetup, 'id' | 'creator'>) => Promise<void> }) {
  const [form, setForm] = useState({ title: '', description: '', date: '', startTime: '', endTime: '', locationName: '', maxParticipants: '8', category: 'SOCIAL', style: 'INFORMAL' as 'INFORMAL' | 'FORMAL' })
  const [saving, setSaving] = useState(false)
  async function submit(event: FormEvent) { event.preventDefault(); setSaving(true); try { await onCreate({ ...form, maxParticipants: Number(form.maxParticipants), latitude: null, longitude: null }) } finally { setSaving(false) } }
  const update = (key: keyof typeof form, value: string) => setForm(old => ({ ...old, [key]: value }))
  return <div className="modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) onClose() }}><form className="modal-card" onSubmit={submit}><button type="button" className="modal-close" onClick={onClose}>×</button><p className="eyebrow">MAKE A PLAN</p><h2>Host a meetup</h2><p className="modal-intro">A small plan can bring the right people together.</p><label>Meetup name<input required value={form.title} onChange={e => update('title', e.target.value)} placeholder="Saturday badminton crew"/></label><label>What’s the plan?<textarea value={form.description} onChange={e => update('description', e.target.value)} rows={3} placeholder="Add a few details people should know"/></label><div className="form-row"><label>Style<select value={form.style} onChange={e => update('style', e.target.value)}><option value="INFORMAL">Casual plan</option><option value="FORMAL">Formal event</option></select></label><label>Category<select value={form.category} onChange={e => update('category', e.target.value)}>{['SPORTS', 'TECHNOLOGY', 'CAREER', 'HEALTH_WELLNESS', 'SOCIAL', 'ARTS', 'OTHER'].map(c => <option key={c} value={c}>{labels[c]}</option>)}</select></label></div><div className="form-row"><label>Date<input type="date" required min={new Date().toISOString().slice(0, 10)} value={form.date} onChange={e => update('date', e.target.value)}/></label><label>Start time<input type="time" required value={form.startTime} onChange={e => update('startTime', e.target.value)}/></label></div><div className="form-row"><label>Location<input required value={form.locationName} onChange={e => update('locationName', e.target.value)} placeholder="Cubbon Park, Bengaluru"/></label><label>Spots<input type="number" min="2" max="100" required value={form.maxParticipants} onChange={e => update('maxParticipants', e.target.value)}/></label></div><button className="dark-button modal-submit" disabled={saving}>{saving ? 'Publishing…' : 'Publish meetup'} <span>↗</span></button></form></div>
}

function ProfileModal({ profile, onClose, onSave }: { profile: Profile; onClose: () => void; onSave: (profile: Pick<Profile, 'name' | 'bio' | 'connectionPurposes'>) => Promise<void> }) {
  const [name, setName] = useState(profile.name ?? '')
  const [bio, setBio] = useState(profile.bio ?? '')
  const [selected, setSelected] = useState<Purpose[]>(profile.connectionPurposes ?? [])
  const [saving, setSaving] = useState(false)
  async function submit(event: FormEvent) { event.preventDefault(); setSaving(true); try { await onSave({ name: name.trim(), bio: bio.trim(), connectionPurposes: selected }) } finally { setSaving(false) } }
  function toggle(purpose: Purpose) { setSelected(old => old.includes(purpose) ? old.filter(value => value !== purpose) : [...old, purpose]) }
  return <div className="modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) onClose() }}><form className="modal-card" onSubmit={submit}><button type="button" className="modal-close" onClick={onClose}>×</button><p className="eyebrow">A LITTLE ABOUT YOU</p><h2>Your profile</h2><p className="modal-intro">Be clear about why you’re here so the right people can find you.</p><label>Your name<input required maxLength={120} value={name} onChange={event => setName(event.target.value)} placeholder="Your name"/></label><label>Short introduction<textarea maxLength={2000} rows={3} value={bio} onChange={event => setBio(event.target.value)} placeholder="What do you enjoy, know, or want to explore?"/></label><fieldset className="purpose-fieldset"><legend>I'm open to connecting for</legend><div className="purpose-choice-grid">{purposeOptions.map(purpose => <label key={purpose} className={`purpose-choice ${selected.includes(purpose) ? 'checked' : ''}`}><input type="checkbox" checked={selected.includes(purpose)} onChange={() => toggle(purpose)}/><span>{selected.includes(purpose) ? '✓' : '+'}</span>{labels[purpose]}</label>)}</div></fieldset><button className="dark-button modal-submit" disabled={saving}>{saving ? 'Saving…' : 'Save profile'} <span>↗</span></button></form></div>
}

function NotificationsPanel({ notifications, unread, onClose, onRead, onReadAll }: { notifications: Notification[]; unread: number; onClose: () => void; onRead: (id: number) => void; onReadAll: () => void }) {
  return <><button className="panel-scrim" aria-label="Close notifications" onClick={onClose}/><aside className="notifications-panel"><div className="panel-heading"><div><p className="eyebrow">YOUR INBOX</p><h2>Notifications {unread > 0 && <span>{unread}</span>}</h2></div><button className="text-button" onClick={onReadAll}>Mark all read</button><button className="modal-close" onClick={onClose}>×</button></div>{notifications.length === 0 ? <Empty title="All quiet for now" copy="Updates about connections and meetups will appear here."/> : notifications.map(item => <button className={`notification-row ${item.readAt ? '' : 'unread'}`} key={item.id} onClick={() => !item.readAt && onRead(item.id)}><span className="notification-icon">✳</span><span><b>{item.title}</b><small>{item.message}</small><time>{new Date(item.createdAt).toLocaleDateString()}</time></span><span className="unread-dot"/></button>)}</aside></> 
}

function PageTitle({ eyebrow, title, copy }: { eyebrow: string; title: string; copy: string }) { return <div className="page-title"><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p>{copy}</p></div> }
function Empty({ title, copy }: { title: string; copy: string }) { return <div className="empty-state"><span>✳</span><h3>{title}</h3><p>{copy}</p></div> }
function Avatar({ name, size = 'normal' }: { name: string; size?: 'normal' | 'large' }) { const initials = name.split(' ').filter(Boolean).slice(0, 2).map(part => part[0]).join('').toUpperCase() || 'F'; return <span className={`avatar ${size === 'large' ? 'avatar-large' : ''}`}>{initials}</span> }

export default App
