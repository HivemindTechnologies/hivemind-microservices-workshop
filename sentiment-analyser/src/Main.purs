module Main where

import Data.Bifunctor (lmap)
import Data.Either (Either(..))
import Data.List.NonEmpty (NonEmptyList)
import Effect (Effect)
import Effect.Aff.Class (liftAff)
import Effect.Class.Console (log)
import Foreign (Foreign, ForeignError)
import Middleware.BodyParser (jsonBodyParser)
import Node.Express.App (App, listenHostHttp, post, get, useExternal)
import Node.Express.Handler (Handler)
import Node.Express.Request (getBody')
import Node.Express.Response (send, sendJson)
import Node.HTTP (Server)
import Prelude (Unit, bind, discard, show, unit, ($), (<>))
import Sentiment (toxicity, toxicity')
import Simple.JSON (class ReadForeign, read)

readIgnoreError ∷ ∀ a. ReadForeign a => Foreign -> Either Unit a
readIgnoreError f = lmap (\_ -> unit) (read f)

type Input
  = { threshold :: Number, sentences :: Array String }

type RequestError
  = { error :: String }

echoHandler :: Handler
echoHandler = do
  body <- getBody'
  case read body :: Either (NonEmptyList ForeignError) Input of
    Left err -> do
      log $ show err
      sendJson $ { error: "invalid request" }
    Right { threshold, sentences } -> do
      output <- liftAff $ toxicity' threshold sentences
      sendJson output

health :: Handler
health = send "{\"status\":\"ok\"}"

app :: App
app = do
  useExternal jsonBodyParser
  post "/toxicity" echoHandler
  get "/" health

main :: Effect Server
main = do
  listenHostHttp app 9003 "0.0.0.0" \_ ->
    log $ "Listening on http://0.0.0.0:" <> show 9003
