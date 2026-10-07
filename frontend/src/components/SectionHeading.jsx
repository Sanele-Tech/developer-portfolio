import { ChevronRight } from 'lucide-react'

export default function SectionHeading({ eyebrow, title, action, to }) {
  return <div className="section-heading"><div><span className="eyebrow">{eyebrow}</span><h2>{title}</h2></div>{action && <a href={to || '#'}>{action} <ChevronRight size={15} /></a>}</div>
}
