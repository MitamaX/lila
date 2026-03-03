package lila.socket

import akka.actor.{ CoordinatedShutdown, Scheduler }
import com.softwaremill.macwire.*
import io.lettuce.core.*
import play.api.Configuration

import lila.core.socket.{ SocketRequester as _, * }

@Module
final class Env(appConfig: Configuration, shutdown: CoordinatedShutdown)(using Executor, Scheduler):

  private val redisClientOpt: Option[RedisClient] =
    appConfig.getOptional[String]("socket.redis.uri").filter(_.nonEmpty).map { uri =>
      RedisClient.create(RedisURI.create(uri))
    }

  val userLag = new UserLagCache
  export userLag.{ getLagRating, put as putLag }

  val requester = new SocketRequester

  val remoteSocket: RemoteSocket = new RemoteSocket(redisClientOpt, shutdown, requester, userLag)
  export remoteSocket.{ kit, parallelKit }

  remoteSocket.subscribe("site-in", RemoteSocket.Protocol.In.baseReader)(remoteSocket.baseHandler)

  val onlineIds = OnlineIds(() => remoteSocket.onlineUserIds.get)

  val isOnline = IsOnline(userId => remoteSocket.onlineUserIds.get contains userId)
