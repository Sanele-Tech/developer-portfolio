import PageHeading from '../../components/PageHeading'
import { Link } from 'react-router-dom'

export default function About() {
  return <section className="page-content">
    <PageHeading eyebrow="About me" title="Building with purpose" />
    <p className="page-intro">I’m a software developer focused on turning practical ideas into dependable digital products. I care about thoughtful engineering, useful experiences, and technology that helps people move forward.</p>
    <div className="about-highlights"><span><strong>01</strong>Systems thinker</span><span><strong>02</strong>Curious builder</span><span><strong>03</strong>Thoughtful collaborator</span></div>
    <div className="about-copy">
      <h3>How I think about software</h3>
      <p>Good software should make a real task feel clearer. I care about the decisions behind the interface: the data model, the trade-offs, the edge cases, and the small details that make a product feel considered.</p>
      <h3>What I do beyond software projects</h3>
      <p>I’m the founder of Varsity Apply, where I lead a small team helping matric students with university and bursary applications. I also take part in skills challenges and am a member of Circuit Breakers Club.</p>
      <Link className="about-more-link" to="/beyond">Explore my work beyond code <span aria-hidden="true">→</span></Link>
    </div>
  </section>
}