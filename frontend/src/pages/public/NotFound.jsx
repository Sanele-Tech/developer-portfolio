import { Link } from 'react-router-dom'
import PageHeading from '../../components/PageHeading'

export default function NotFound() { return <section className="page-content"><PageHeading eyebrow="404" title="Page not found" /><p className="page-intro">The page you requested does not exist.</p><Link className="button button-dark" to="/">Return home</Link></section> }