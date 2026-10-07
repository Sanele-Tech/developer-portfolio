export default function EmptyState({ title = 'Nothing here yet', message = 'This section will be populated from the admin dashboard.' }) {
  return <div className="empty-state"><h3>{title}</h3><p>{message}</p></div>
}
