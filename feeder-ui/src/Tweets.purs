module Tweets where

import Prelude
import Affjax as AX
import Affjax.RequestBody as RequestBody
import Affjax.ResponseFormat as ResponseFormat
import Data.Array as Array
import Data.Bifunctor (lmap)
import Data.Either (Either(..))
import Data.Foldable (traverse_)
import Data.Maybe (Maybe(..))
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Console (log)
import React.Basic.DOM as R
import React.Basic.DOM.Events (preventDefault, stopPropagation, targetValue)
import React.Basic.Events (handler)
import React.Basic.Hooks (ReactComponent, component, element, useState, (/\))
import React.Basic.Hooks as React
import Simple.JSON as JSON
import AffReducer (useAffReducer)
import Effect.Aff (Aff)

data Action
  = SendTweet String

type Tweet
  = { content :: String
    }

type State
  = { tweets :: Array Tweet
    }

reducer :: String -> State -> Action -> { state :: State, effects :: Array (Aff (Array Action)) }
reducer url state (SendTweet content) = { state: newState, effects: [ effects ] }
  where
  newState = state { tweets = Array.cons { content } state.tweets }

  effects = do
    _ <- liftEffect $ log $ "Sending tweet " <> content
    maybeResponse <- AX.post ResponseFormat.string (url <> "/api/tweet") (Just (RequestBody.string (JSON.writeJSON { content: content }))) <#> lmap (AX.printError)
    _ <-
      liftEffect $ log
        $ case maybeResponse of
            Left err -> "Failed to post tweet " <> content <> "\n" <> err
            Right response -> show response.body
    pure []

mkTweetUI :: String -> Effect (ReactComponent {})
mkTweetUI url = do
  let
    initialState = { tweets: [] }
  tweetInput <- mkTweetInput
  tweetRow <- mkTweetRow
  component "Tweets" \props -> React.do
    state /\ dispatch <- useAffReducer initialState $ reducer url
    pure
      $ R.div
          { children:
            [ element tweetInput { dispatch }
            , R.div_
                $ flip map state.tweets \tweet ->
                    element tweetRow { tweet }
            ]
          , className: "container"
          }

mkTweetInput :: Effect (ReactComponent { dispatch :: Action -> Effect Unit })
mkTweetInput = do
  component "TweetInput" \props -> React.do
    tweet /\ setTweet <- useState ""
    let
      sendTweet =
        handler (preventDefault >>> stopPropagation) \_ -> do
          props.dispatch $ SendTweet tweet
          setTweet $ const ""

      updateTweetInput =
        handler (preventDefault >>> stopPropagation >>> targetValue)
          $ traverse_ (setTweet <<< const)
    pure
      $ R.form
          { onSubmit: sendTweet
          , children:
            [ R.input
                { value : tweet 
                , onChange: updateTweetInput
                , className: "tweet"
                , placeholder: "Tweet something"
                }
            ]
          , className: "tweet"
          }

mkTweetRow :: Effect (ReactComponent { tweet :: Tweet })
mkTweetRow =
  component "Tweet" \props -> React.do
    pure
      $ R.div
          { children:
            [ R.label
                { children:
                  [ R.text props.tweet.content
                  ]
                , className: "tweet"
                }
            ]
          , className: "tweet"
          }
