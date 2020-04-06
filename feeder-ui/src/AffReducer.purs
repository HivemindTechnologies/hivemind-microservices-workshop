module AffReducer where

import Prelude
import Data.Foldable (for_)
import Data.Newtype (class Newtype)
import Effect.Aff (Aff, launchAff_)
import React.Basic.Hooks (type (/\), Hook, UnsafeReference(..), UseEffect, UseReducer, coerceHook, useEffect, useReducer, (/\))
import Effect (Effect)
import Effect.Class (liftEffect)
import React.Basic.Hooks as React

-- Remove once https://github.com/spicydonuts/purescript-react-basic-hooks/pull/20 is merged
newtype UseAffReducer state action hooks
  = UseAffReducer
  ( UseEffect (UnsafeReference (Array (Aff (Array action))))
      (UseReducer { state :: state, effects :: Array (Aff (Array action)) } action hooks)
  )

derive instance ntUseAffReducer :: Newtype (UseAffReducer state action hooks) _

-- | Provide an initial state and a reducer function. This is a more powerful
-- | version of `useReducer`, where a state change can additionally queue
-- | asynchronous operations. The results of those operations must be  mapped
-- | into the reducer's `action` type. This is essentially the Elm architecture.
-- |
-- | *Note: Aff failures are thrown. If you need to capture an error state, be
-- |   sure to capture it in your action type!*
useAffReducer ::
  forall state action.
  state ->
  (state -> action -> { state :: state, effects :: Array (Aff (Array action)) }) ->
  Hook (UseAffReducer state action) (state /\ (action -> Effect Unit))
useAffReducer initialState reducer =
  coerceHook React.do
    { state, effects } /\ dispatch <-
      useReducer { state: initialState, effects: [] } (_.state >>> reducer)
    useEffect (UnsafeReference effects) do
      for_ effects \aff ->
        launchAff_ do
          actions <- aff
          liftEffect do for_ actions dispatch
      mempty
    pure (state /\ dispatch)