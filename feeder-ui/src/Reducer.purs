module Reducer where

import Prelude
import Affjax as AX
import Affjax as Ax
import Affjax.RequestBody as RequestBody
import Affjax.ResponseFormat as ResponseFormat
import Data.Array as Array
import Data.Bifunctor (lmap)
import Data.Either (Either(..))
import Data.Foldable (traverse_)
import Data.Formatter.DateTime (formatDateTime)
import Data.Maybe (Maybe(..))
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Console (log)
import Effect.Now as Now
import React.Basic.DOM as R
import React.Basic.DOM.Events (preventDefault, stopPropagation, targetValue)
import React.Basic.Events (handler)
import React.Basic.Hooks (ReactComponent, component, element, memo, useReducer, useState, (/\))
import React.Basic.Hooks as React
import React.Basic.Hooks.Aff (useAff)
import Simple.JSON as JSON

data Action
  = SendTweet String

type Tweet
  = { content :: String
    }

type TweetWithTs
  = { content :: String
    , timestamp :: String
    }

type State
  = { posted :: Maybe Tweet
    , tweets :: Array Tweet
    }

reducer :: State -> Action -> State
reducer state (SendTweet content) = state { posted = Just { content }, tweets = Array.cons { content } state.tweets }

mkReducer :: String -> Effect (ReactComponent {})
mkReducer url = do
  let
    initialState = { posted: Nothing, tweets: [] }
  tweetInput <- memo mkTweetInput
  tweetRow <- memo mkTweetRow
  component "Tweets" \props -> React.do
    state /\ dispatch <- useReducer initialState reducer
    -- There is probably a way to use just one hook for posting & updating state, haven't figured out though
    useAff state do
      liftEffect $ log $ "State: " <> (show state)
      case state.posted of
        Just tweet -> do
          maybeTs <- (formatDateTime "YYYY-MM-DDTHH:mm:ssZ") <$> Now.nowDateTime # liftEffect
          maybeResponse <- case maybeTs of
            Right ts -> AX.post ResponseFormat.string (url <> "/api/tweet") (Just (RequestBody.string (JSON.writeJSON { content: tweet.content, timestamp: ts }))) <#> lmap (Ax.printError)
            Left err -> pure $ Left err
          case maybeResponse of
            Left err -> liftEffect $ log $ "Failed to post tweet" <> err
            Right response -> liftEffect $ log $ show response.body
        Nothing -> pure unit
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
    value /\ setValue <- useState ""
    pure
      $ R.form
          { onSubmit:
            handler (preventDefault >>> stopPropagation) \_ -> do
              props.dispatch $ SendTweet value
              setValue $ const ""
          , children:
            [ R.input
                { value
                , onChange:
                  handler (preventDefault >>> stopPropagation >>> targetValue)
                    $ traverse_ (setValue <<< const)
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
