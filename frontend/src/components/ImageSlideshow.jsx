import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useEffect, useState } from 'react'

export default function ImageSlideshow({ images, alt, className = '' }) {
  const [activeIndex, setActiveIndex] = useState(0)
  const [paused, setPaused] = useState(false)
  const currentIndex = images.length ? activeIndex % images.length : 0

  useEffect(() => {
    if (paused || images.length < 2) return undefined
    const timer = window.setInterval(() => {
      setActiveIndex((index) => (index + 1) % images.length)
    }, 5000)
    return () => window.clearInterval(timer)
  }, [images.length, paused])

  function moveBy(offset) {
    setActiveIndex((index) => (index + offset + images.length) % images.length)
  }

  function handleNavigation(event, offset) {
    event.preventDefault()
    event.stopPropagation()
    moveBy(offset)
  }

  if (!images.length) return null

  return <div className={`image-slideshow ${className}`} onMouseEnter={() => setPaused(true)}
    onMouseLeave={() => setPaused(false)} onFocusCapture={() => setPaused(true)}
    onBlurCapture={(event) => {
      if (!event.currentTarget.contains(event.relatedTarget)) setPaused(false)
    }}>
    <img src={images[currentIndex]} alt={alt} loading="lazy" />
    {images.length > 1 && <>
      <button className="image-slide-arrow is-previous" type="button" aria-label="Show previous image" onClick={(event) => handleNavigation(event, -1)}>
        <ChevronLeft size={18} />
      </button>
      <button className="image-slide-arrow is-next" type="button" aria-label="Show next image" onClick={(event) => handleNavigation(event, 1)}>
        <ChevronRight size={18} />
      </button>
      <div className="image-slide-dots" aria-label={`Image ${currentIndex + 1} of ${images.length}`}>
        {images.map((image, index) => <button key={`${image}-${index}`} type="button"
          aria-label={`Show image ${index + 1}`} aria-current={index === currentIndex ? 'true' : undefined}
          onClick={(event) => {
            event.preventDefault()
            event.stopPropagation()
            setActiveIndex(index)
          }} />)}
      </div>
    </>}
  </div>
}
