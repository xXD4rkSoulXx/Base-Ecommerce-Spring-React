import { describe, expect, it } from 'vitest'
import { reviewSchema } from '@/schemas/review'
import { issuesOf } from '@/test/schema'

const validReview = { rating: 4, title: 'Great fit', description: 'Fits as expected and keeps me dry.' }

describe('reviewSchema', () => {
  it.each([1, 2, 3, 4, 5])('accepts a rating of %s', (rating) => {
    expect(issuesOf(reviewSchema, { ...validReview, rating })).toEqual([])
  })

  it('asks for a rating when it is zero', () => {
    expect(issuesOf(reviewSchema, { ...validReview, rating: 0 })).toEqual([
      { path: 'rating', message: 'Select a rating' }
    ])
  })

  it.each([6, 3.5])('rejects the rating %s', (rating) => {
    expect(issuesOf(reviewSchema, { ...validReview, rating }).map((issue) => issue.path)).toEqual(['rating'])
  })

  it('requires a title', () => {
    expect(issuesOf(reviewSchema, { ...validReview, title: '  ' })).toEqual([
      { path: 'title', message: 'Title is required' }
    ])
  })

  it('rejects a title with more than 100 characters', () => {
    expect(issuesOf(reviewSchema, { ...validReview, title: 'x'.repeat(101) })).toEqual([
      { path: 'title', message: 'Title must be at most 100 characters' }
    ])
  })

  it('requires a description', () => {
    expect(issuesOf(reviewSchema, { ...validReview, description: '' })).toEqual([
      { path: 'description', message: 'Description is required' }
    ])
  })

  it('accepts a description with exactly 2000 characters', () => {
    expect(issuesOf(reviewSchema, { ...validReview, description: 'x'.repeat(2000) })).toEqual([])
  })

  it('rejects a description with more than 2000 characters', () => {
    expect(issuesOf(reviewSchema, { ...validReview, description: 'x'.repeat(2001) })).toEqual([
      { path: 'description', message: 'Description must be at most 2000 characters' }
    ])
  })
})
