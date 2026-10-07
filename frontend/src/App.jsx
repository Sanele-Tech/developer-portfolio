import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import PublicLayout from './layouts/PublicLayout'
import Home from './pages/public/Home'
import About from './pages/public/About'
import Contact from './pages/public/Contact'
import ProjectDetail from './pages/public/ProjectDetail'
import NotFound from './pages/public/NotFound'
import CmsPage from './pages/public/CmsPage'
import PublishedPages from './pages/public/PublishedPages'
import Certifications from './pages/public/Certifications'
import Hackathons from './pages/public/Hackathons'
import BeyondCode from './pages/public/BeyondCode'
import { CollectionPage, ProjectsPage } from './pages/public/CollectionPage'
import AdminAuthProvider from './admin/AdminAuthProvider'
import AdminLayout from './layouts/AdminLayout'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminLogin from './pages/admin/AdminLogin'
import AdminProjects from './pages/admin/AdminProjects'
import AdminProjectForm from './pages/admin/AdminProjectForm'
import AdminExperience from './pages/admin/AdminExperience'
import AdminExperienceForm from './pages/admin/AdminExperienceForm'
import AdminEducation from './pages/admin/AdminEducation'
import AdminEducationForm from './pages/admin/AdminEducationForm'
import AdminSkills from './pages/admin/AdminSkills'
import AdminSkillForm from './pages/admin/AdminSkillForm'
import AdminAchievements from './pages/admin/AdminAchievements'
import AdminAchievementForm from './pages/admin/AdminAchievementForm'
import AdminDocuments from './pages/admin/AdminDocuments'
import AdminDocumentForm from './pages/admin/AdminDocumentForm'
import AdminPages from './pages/admin/AdminPages'
import AdminPageForm from './pages/admin/AdminPageForm'
import AdminSettings from './pages/admin/AdminSettings'
import AdminSettingForm from './pages/admin/AdminSettingForm'
import AdminAuditLogs from './pages/admin/AdminAuditLogs'
import AdminContactMessages from './pages/admin/AdminContactMessages'
import AdminEmailTemplates from './pages/admin/AdminEmailTemplates'
import AdminEmailDeliveries from './pages/admin/AdminEmailDeliveries'
import AdminAnalytics from './pages/admin/AdminAnalytics'
import './App.css'
import './admin/admin.css'
import './public.css'
function App() {
  return <BrowserRouter><Routes>
    <Route element={<PublicLayout />}>
      <Route path="/" element={<Home />} />
      <Route path="/about" element={<About />} />
      <Route path="/projects" element={<ProjectsPage />} />
      <Route path="/projects/:slug" element={<ProjectDetail />} />
      <Route path="/experience" element={<CollectionPage type="experience" />} />
      <Route path="/education" element={<CollectionPage type="education" />} />
      <Route path="/skills" element={<CollectionPage type="skills" />} />
      <Route path="/achievements" element={<CollectionPage type="achievements" />} />
      <Route path="/certifications" element={<Certifications />} />
      <Route path="/hackathons" element={<Hackathons />} />
      <Route path="/beyond" element={<BeyondCode />} />
      <Route path="/contact" element={<Contact />} />
      <Route path="/pages" element={<PublishedPages />} />
      <Route path="/pages/:slug" element={<CmsPage />} />
      <Route path="/home" element={<Navigate to="/" replace />} />
      <Route path="*" element={<NotFound />} />
    </Route>
    <Route path="/admin/login" element={<AdminLogin />} />
    <Route path="/admin" element={<AdminAuthProvider><AdminLayout /></AdminAuthProvider>}>
      <Route index element={<AdminDashboard />} />
      <Route path="projects" element={<AdminProjects />} />
      <Route path="projects/new" element={<AdminProjectForm />} />
      <Route path="projects/:id/edit" element={<AdminProjectForm />} />
      <Route path="experience" element={<AdminExperience />} />
      <Route path="experience/new" element={<AdminExperienceForm />} />
      <Route path="experience/:id/edit" element={<AdminExperienceForm />} />
      <Route path="education" element={<AdminEducation />} />
      <Route path="education/new" element={<AdminEducationForm />} />
      <Route path="education/:id/edit" element={<AdminEducationForm />} />
      <Route path="skills" element={<AdminSkills />} />
      <Route path="skills/new" element={<AdminSkillForm />} />
      <Route path="skills/:id/edit" element={<AdminSkillForm />} />
      <Route path="achievements" element={<AdminAchievements />} />
      <Route path="achievements/new" element={<AdminAchievementForm />} />
      <Route path="achievements/:id/edit" element={<AdminAchievementForm />} />
      <Route path="documents" element={<AdminDocuments />} />
      <Route path="documents/new" element={<AdminDocumentForm />} />
      <Route path="documents/:id/edit" element={<AdminDocumentForm />} />
      <Route path="pages" element={<AdminPages />} />
      <Route path="pages/new" element={<AdminPageForm />} />
      <Route path="pages/:id/edit" element={<AdminPageForm />} />
      <Route path="settings" element={<AdminSettings />} />
      <Route path="settings/new" element={<AdminSettingForm />} />
      <Route path="settings/:key/edit" element={<AdminSettingForm />} />
      <Route path="audit-logs" element={<AdminAuditLogs />} />
      <Route path="inbox" element={<AdminContactMessages />} />
      <Route path="email-templates" element={<AdminEmailTemplates />} />
      <Route path="email-deliveries" element={<AdminEmailDeliveries />} />
      <Route path="analytics" element={<AdminAnalytics />} />
    </Route>
  </Routes></BrowserRouter>
}

export default App
