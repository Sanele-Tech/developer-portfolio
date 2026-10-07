export default function ExpandableText({ text, className = '', threshold = 170 }) {
  if (!text) return null
  if (text.length <= threshold) return <p className={className}>{text}</p>

  return <details className={`expandable-copy ${className}`}>
    <summary><span className="expandable-copy-preview">{text}</span><span className="expandable-copy-more">Read more</span><span className="expandable-copy-less">Show less</span></summary>
    <p>{text}</p>
  </details>
}
