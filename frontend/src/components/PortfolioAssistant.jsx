import { useEffect, useRef, useState } from 'react'
import { Bot, MessageCircle, Send, X } from 'lucide-react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../services/api'

const portfolioSources = [
  ['/api/profile', 'Profile'],
  ['/api/projects', 'Projects'],
  ['/api/experience', 'Experience'],
  ['/api/education', 'Education'],
  ['/api/skills', 'Skills'],
  ['/api/achievements', 'Certifications and achievements'],
  ['/api/pages', 'Published pages'],
]

const knownDetails = [
  {
    category: 'Profile',
    title: 'Sanele Dlomo',
    text: 'Sanele is a software developer focused on building practical, dependable digital products and is studying ICT Applications Development.',
    href: '/about',
  },
]

const suggestions = [
  'What projects has Sanele built?',
  'Tell me about Varsity Apply',
  'Which hackathon did Sanele join?',
  'What certifications are listed?',
]

export default function PortfolioAssistant() {
  const [open, setOpen] = useState(false)
  const [question, setQuestion] = useState('')
  const [messages, setMessages] = useState([{ role: 'assistant', text: 'Hi, I’m Sanele’s portfolio assistant. I can help you explore his projects, skills, studies, certifications, hackathons, and community work. What would you like to know?' }])
  const [documents, setDocuments] = useState(null)
  const [indexWarning, setIndexWarning] = useState('')
  const [indexError, setIndexError] = useState('')
  const [loading, setLoading] = useState(false)
  const [isMobile, setIsMobile] = useState(false)
  const inputRef = useRef(null)
  const threadRef = useRef(null)
  const assistantRef = useRef(null)
  const conversationContext = useRef([])

  useEffect(() => {
    if (!open || documents) return undefined
    let active = true
    Promise.allSettled(portfolioSources.map(([endpoint]) => apiFetch(endpoint)))
      .then((results) => {
        if (!active) return
        const loaded = []
        let failures = 0
        results.forEach((result, index) => {
          if (result.status === 'fulfilled') {
            loaded.push(...documentsFromSource(portfolioSources[index][1], result.value))
          } else {
            failures += 1
          }
        })
        const allDocuments = [...knownDetails, ...loaded]
        setDocuments(allDocuments)
        setIndexWarning(failures ? `${failures} portfolio section${failures === 1 ? '' : 's'} could not be loaded, so answers may be incomplete.` : '')
        if (!loaded.length) setIndexError('Portfolio information could not be loaded. Check your connection and try reopening the assistant.')
      })
      .catch((error) => {
        console.error('Unable to load portfolio information for the assistant.', error)
        if (active) setIndexError('Portfolio information could not be loaded. Check your connection and try reopening the assistant.')
      })
    return () => { active = false }
  }, [open, documents])

  useEffect(() => {
    const thread = threadRef.current
    if (thread) thread.scrollTop = thread.scrollHeight
  }, [messages, loading, open])

  useEffect(() => {
    if (open && !window.matchMedia('(max-width: 600px)').matches) inputRef.current?.focus()
  }, [open])

  useEffect(() => {
    function closeOnEscape(event) {
      if (event.key === 'Escape') setOpen(false)
    }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [])

  useEffect(() => {
    const mobileQuery = window.matchMedia('(max-width: 600px)')
    const updateMobile = () => setIsMobile(mobileQuery.matches)
    updateMobile()
    mobileQuery.addEventListener('change', updateMobile)
    return () => mobileQuery.removeEventListener('change', updateMobile)
  }, [])

  useEffect(() => {
    if (!open) return undefined
    document.body.classList.add('portfolio-assistant-open')

    const assistant = assistantRef.current
    const viewport = window.visualViewport
    const updateViewport = () => {
      if (!assistant || !viewport) return
      assistant.style.setProperty('--assistant-viewport-height', `${viewport.height}px`)
      assistant.style.setProperty('--assistant-viewport-top', `${viewport.offsetTop}px`)
    }
    updateViewport()
    viewport?.addEventListener('resize', updateViewport)
    viewport?.addEventListener('scroll', updateViewport)

    return () => {
      document.body.classList.remove('portfolio-assistant-open')
      viewport?.removeEventListener('resize', updateViewport)
      viewport?.removeEventListener('scroll', updateViewport)
      assistant?.style.removeProperty('--assistant-viewport-height')
      assistant?.style.removeProperty('--assistant-viewport-top')
    }
  }, [open])

  function ask(value) {
    const prompt = value.trim()
    if (!prompt || loading || documents === null) return
    setQuestion('')
    setMessages((current) => [...current, { role: 'user', text: prompt }])
    setLoading(true)
    window.setTimeout(() => {
      const answer = answerFromPortfolio(prompt, documents, conversationContext.current)
      conversationContext.current = answer.context || []
      setMessages((current) => [...current, answer])
      setLoading(false)
    }, 120)
  }

  function submit(event) {
    event.preventDefault()
    ask(question)
  }

  return <div className={`portfolio-assistant${open ? ' is-open' : ''}`} ref={assistantRef}>
    {open && <section className="assistant-panel" role="dialog" aria-modal={isMobile ? 'true' : 'false'} aria-labelledby="assistant-title">
      <header className="assistant-header">
        <span className="assistant-avatar"><Bot size={19} /></span>
        <div><strong id="assistant-title">Sanele’s portfolio assistant</strong><small>Portfolio-aware · Replies use published content</small></div>
        <button type="button" aria-label="Close assistant" onClick={() => setOpen(false)}><X size={18} /></button>
      </header>
      <div className="assistant-thread" ref={threadRef} aria-live="polite">
        {indexWarning && <p className="assistant-index-note" role="status">{indexWarning}</p>}
        {indexError && <p className="assistant-index-error" role="status">{indexError} <button type="button" onClick={() => {
          setDocuments(null)
          setIndexError('')
          setIndexWarning('')
        }}>Try again</button></p>}
        {messages.map((message, index) => <article className={`assistant-message is-${message.role}`} key={`${message.role}-${index}`}>
          {message.text && <p>{message.text}</p>}
          {message.items?.map((item) => <div className="assistant-result" key={`${item.category}-${item.title}`}>
            <span>{item.category}</span><strong>{item.title}</strong><p>{item.text}</p>
            {item.href && <Link to={item.href} onClick={() => setOpen(false)}>View details</Link>}
            {item.url && <a href={item.url} target="_blank" rel="noreferrer">Open link</a>}
          </div>)}
        </article>)}
        {documents === null && !indexError && <p className="assistant-typing" role="status">Getting the portfolio ready…</p>}
        {loading && <p className="assistant-typing" role="status">Thinking…</p>}
      </div>
      {messages.length === 1 && <div className="assistant-suggestions">{suggestions.map((suggestion) => <button type="button" key={suggestion} disabled={documents === null || loading} onClick={() => ask(suggestion)}>{suggestion}</button>)}</div>}
      <form className="assistant-input" onSubmit={submit}>
        <input ref={inputRef} value={question} onChange={(event) => setQuestion(event.target.value)} maxLength={240} placeholder={documents === null ? 'Preparing portfolio…' : 'Ask a question or follow up…'} aria-label="Ask the portfolio assistant" disabled={documents === null || Boolean(indexError)} />
        <button type="submit" aria-label="Send question" disabled={!question.trim() || loading || documents === null || Boolean(indexError)}><Send size={17} /></button>
      </form>
      <p className="assistant-disclaimer">I answer from Sanele’s published portfolio. I may not know details that aren’t listed.</p>
    </section>}
    <button className="assistant-launcher" type="button" aria-expanded={open} aria-label={open ? 'Close portfolio assistant' : 'Open portfolio assistant'} onClick={() => setOpen((current) => !current)}>
      {open ? <X size={21} /> : <><MessageCircle size={19} /><span>Ask about my work</span></>}
    </button>
  </div>
}

function documentsFromSource(category, data) {
  const items = Array.isArray(data) ? data : [data]
  return items.filter((item) => item && typeof item === 'object').map((item) => {
    const itemCategory = category === 'Certifications and achievements'
      ? item.cardType === 'BUSINESS' || item.cardType === 'COMMUNITY' || item.cardType === 'OTHER'
        ? 'Business and community'
        : item.cardType === 'CHALLENGE'
          ? 'Hackathons'
          : category
      : category
    const title = firstText(item, ['title', 'name', 'jobTitle', 'qualification', 'institution', 'slug']) || category
    const description = firstText(item, ['description', 'summary', 'shortDescription', 'bio', 'content'])
    const details = [
      firstText(item, ['issuer', 'company', 'institution', 'course', 'degree', 'duration', 'location', 'category', 'proficiency', 'achievedOn', 'startDate', 'endDate']),
      listText([item.technologies, item.focus, item.tags, item.coreModules, item.modules, item.interests]
        .filter((value) => Array.isArray(value) && value.length)
        .flat()),
      description,
    ].filter(Boolean).join(' · ')
    return {
      category: itemCategory,
      title,
      text: details || `${title} is listed in Sanele’s public ${category.toLowerCase()} portfolio.`,
      url: category === 'Certifications and achievements' ? firstText(item, ['url']) : '',
      href: internalHref(itemCategory, item),
    }
  })
}

function firstText(item, fields) {
  for (const field of fields) {
    const value = item[field]
    if (typeof value === 'string' && value.trim()) return value.trim()
    if (typeof value === 'number') return String(value)
  }
  return ''
}

function listText(value) {
  if (!Array.isArray(value)) return ''
  return value.map((item) => typeof item === 'string' ? item : firstText(item, ['name', 'title', 'technology', 'techName'])).filter(Boolean).join(', ')
}

function internalHref(category, item) {
  if (category === 'Projects' && item.slug) return `/projects/${encodeURIComponent(item.slug)}`
  if (category === 'Published pages' && item.slug) return `/pages/${encodeURIComponent(item.slug)}`
  const paths = {
    Profile: '/about',
    Projects: '/projects',
    Experience: '/experience',
    Education: '/education',
    Skills: '/skills',
    'Certifications and achievements': '/certifications',
    'Business and community': '/beyond',
    Hackathons: '/hackathons',
    'Published pages': '/pages',
  }
  return paths[category] || ''
}

function answerFromPortfolio(question, documents, previousContext = []) {
  const terms = tokenize(question)
  const normalizedQuestion = normalize(question)
  if (/^(hi|hello|hey|good morning|good afternoon|good evening|how are you)\b/.test(normalizedQuestion)) {
    return { role: 'assistant', text: 'Hi! I’m glad you stopped by. Are you curious about a project, Sanele’s experience, or something he does outside coding?', context: previousContext }
  }
  if (/^(thanks|thank you|thx|cheers)\b/.test(normalizedQuestion)) {
    return { role: 'assistant', text: 'You’re welcome! Feel free to ask a follow-up or explore another part of the portfolio.', context: previousContext }
  }
  if (/\b(what can you do|how can you help|who are you)\b/.test(normalizedQuestion)) {
    return { role: 'assistant', text: 'I can help you explore Sanele’s published portfolio, including his projects, skills, education, experience, certifications, hackathons, and community work. Ask naturally, and you can follow up about something we’ve discussed.', context: previousContext }
  }

  const isFollowUp = /\b(it|that|this|they|them|he|his|those|more|another|there|which one|tell me more|what about)\b/.test(normalizedQuestion)
  const asksForLink = /\b(link|where can i|view it|see it|open it)\b/.test(normalizedQuestion)
  if (isFollowUp && previousContext.length) {
    const matchingContext = terms.length
      ? previousContext.filter((item) => scoreDocument(item, terms, normalizedQuestion) > 0)
      : previousContext
    const contextItems = matchingContext.length ? matchingContext : previousContext
    if (asksForLink) {
      const linkedItems = contextItems.filter((item) => item.href || item.url)
      if (linkedItems.length) {
        return {
          role: 'assistant',
          text: `You can explore ${linkedItems[0].title} here:`,
          items: linkedItems.slice(0, 2),
          context: linkedItems,
        }
      }
    }
    return {
      role: 'assistant',
      text: `Sure — here’s a little more about ${contextItems[0].title}:`,
      items: contextItems.slice(0, 3),
      context: contextItems,
    }
  }

  const topic = [
    ['Profile', ['who is', 'about sanele', 'background', 'tell me about him', 'tell me about sanele']],
    ['certification', ['certification', 'certificate', 'credential', 'course', 'certified']],
    ['Hackathons', ['hackathon', 'challenge', 'competition']],
    ['Business and community', ['varsity', 'business', 'matric', 'bursary', 'circuit', 'club', 'community', 'volunteer', 'outside coding', 'outside academics', 'interest', 'hobby']],
    ['Skills', ['skill', 'technology', 'tech stack', 'tools', 'programming language']],
    ['Projects', ['project', 'built', 'build', 'work', 'app', 'application']],
    ['Experience', ['experience', 'job', 'career', 'employment']],
    ['Education', ['education', 'study', 'university', 'qualification', 'degree', 'coursework']],
  ].find(([, keywords]) => keywords.some((keyword) => normalizedQuestion.includes(keyword)))

  const candidates = documents
    .filter((item) => !topic || (topic[0] === 'certification'
      ? item.category === 'Certifications and achievements'
      : item.category === topic[0]))
    .map((item) => ({ item, score: scoreDocument(item, terms, normalizedQuestion) }))
    .filter(({ score }) => score > 0)
    .sort((left, right) => right.score - left.score)

  const matches = (topic?.[0] === 'Profile'
    ? documents.filter((item) => item.category === 'Profile').slice(0, 1)
    : terms.length
    ? candidates.slice(0, topic ? 5 : 3).map(({ item }) => item)
    : documents.filter((item) => item.category === 'Profile').slice(0, 1))
  if (!matches.length) {
    return {
      role: 'assistant',
      text: 'I couldn’t find that detail in the portfolio, and I don’t want to guess. Could you rephrase it or ask about a project, skill, experience, education, certification, hackathon, or community activity?',
      context: previousContext,
    }
  }
  const category = matches[0].category.toLowerCase()
  const titleMatch = matches.find((item) => normalizedQuestion.includes(normalize(item.title)))
  const introduction = titleMatch
    ? `Here’s what Sanele’s portfolio says about ${titleMatch.title}:`
    : topic
      ? `Here’s what I found about Sanele’s ${category}:`
      : 'Here are a few relevant details from Sanele’s portfolio:'
  return {
    role: 'assistant',
    text: introduction,
    items: matches,
    context: matches,
  }
}

function scoreDocument(item, terms, normalizedQuestion) {
  const title = normalize(item.title)
  const content = normalize(`${item.title} ${item.text} ${item.category}`)
  let score = normalizedQuestion.includes(title) ? 5 : 0
  for (const term of terms) {
    if (title.includes(term)) score += 3
    else if (content.includes(term)) score += 1
  }
  return score
}

function tokenize(value) {
  const ignored = new Set(['about', 'what', 'which', 'where', 'when', 'does', 'did', 'have', 'has', 'tell', 'show', 'list', 'find', 'with', 'from', 'that', 'this', 'they', 'them', 'there', 'sanele', 'please', 'work'])
  return normalize(value).split(/[^a-z0-9]+/).filter((word) => word.length > 2 && !ignored.has(word))
}

function normalize(value) {
  return String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
}
