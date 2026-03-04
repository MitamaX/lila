package lila.app

import com.softwaremill.macwire.*
import play.api.libs.ws.StandaloneWSClient
import play.api.mvc.{ ControllerComponents, SessionCookieBaker }
import play.api.{ Configuration, Environment, Mode }

import lila.core.config.*
import lila.common.config.GetRelativeFile

final class Env(
    val config: Configuration,
    val controllerComponents: ControllerComponents,
    environment: Environment,
    shutdown: akka.actor.CoordinatedShutdown,
    cookieBaker: SessionCookieBaker
)(using val system: akka.actor.ActorSystem, val executor: Executor)(using
    StandaloneWSClient,
    akka.stream.Materializer
):
  val net: NetConfig = lila.web.WebConfig.netConfig(config)
  export net.baseUrl
  val routeUrl: RouteUrl = call => Url(s"${baseUrl}${call.url}")

  given mode: Mode = environment.mode
  given translator: lila.core.i18n.Translator = lila.i18n.Translator
  given scheduler: Scheduler = system.scheduler
  given RateLimit = net.rateLimit
  given NetDomain = net.domain
  val getFile: GetRelativeFile = GetRelativeFile(environment.getFile(_))

  // wire all the lila modules in the right order — all lazy to defer DB access
  val i18n: lila.i18n.Env.type = lila.i18n.Env
  lazy val mongo: lila.db.Env = wire[lila.db.Env]
  lazy val memo: lila.memo.Env = wire[lila.memo.Env]
  lazy val socket: lila.socket.Env = wire[lila.socket.Env]
  lazy val user: lila.user.Env = wire[lila.user.Env]
  lazy val mailer: lila.mailer.Env = wire[lila.mailer.Env]
  lazy val oAuth: lila.oauth.Env = wire[lila.oauth.Env]
  lazy val security: lila.security.Env = wire[lila.security.Env]
  lazy val pref: lila.pref.Env = wire[lila.pref.Env]
  lazy val relation: lila.relation.Env = wire[lila.relation.Env]
  lazy val title: lila.title.Env = wire[lila.title.Env]
  lazy val game: lila.game.Env = wire[lila.game.Env]
  import game.given
  lazy val notifyM: lila.notify.Env = wire[lila.notify.Env]
  lazy val irc: lila.irc.Env = wire[lila.irc.Env]
  lazy val report: lila.report.Env = wire[lila.report.Env]
  lazy val shutup: lila.shutup.Env = wire[lila.shutup.Env]
  lazy val chat: lila.chat.Env = wire[lila.chat.Env]
  lazy val msg: lila.msg.Env = wire[lila.msg.Env]
  lazy val playban: lila.playban.Env = wire[lila.playban.Env]
  lazy val evalCache: lila.evalCache.Env = wire[lila.evalCache.Env]
  lazy val analyse: lila.analyse.Env = wire[lila.analyse.Env]
  lazy val fishnet: lila.fishnet.Env = wire[lila.fishnet.Env]
  lazy val history: lila.history.Env = wire[lila.history.Env]
  lazy val bookmark: lila.bookmark.Env = wire[lila.bookmark.Env]
  lazy val round: lila.round.Env = wire[lila.round.Env]
  lazy val search: lila.search.Env = wire[lila.search.Env]
  lazy val gameSearch: lila.gameSearch.Env = wire[lila.gameSearch.Env]
  lazy val perfStat: lila.perfStat.Env = wire[lila.perfStat.Env]
  lazy val tournament: lila.tournament.Env = wire[lila.tournament.Env]
  lazy val swiss: lila.swiss.Env = wire[lila.swiss.Env]
  lazy val mod: lila.mod.Env = wire[lila.mod.Env]
  lazy val team: lila.team.Env = wire[lila.team.Env]
  lazy val teamSearch: lila.teamSearch.Env = wire[lila.teamSearch.Env]
  lazy val forum: lila.forum.Env = wire[lila.forum.Env]
  lazy val forumSearch: lila.forumSearch.Env = wire[lila.forumSearch.Env]
  lazy val pool: lila.pool.Env = wire[lila.pool.Env]
  import pool.given
  lazy val lobby: lila.lobby.Env = wire[lila.lobby.Env]
  lazy val setup: lila.setup.Env = wire[lila.setup.Env]
  lazy val simul: lila.simul.Env = wire[lila.simul.Env]
  lazy val appeal: lila.appeal.Env = wire[lila.appeal.Env]
  lazy val timeline: lila.timeline.Env = wire[lila.timeline.Env]
  lazy val puzzle: lila.puzzle.Env = wire[lila.puzzle.Env]
  lazy val coordinate: lila.coordinate.Env = wire[lila.coordinate.Env]
  lazy val tv: lila.tv.Env = wire[lila.tv.Env]
  lazy val feed: lila.feed.Env = wire[lila.feed.Env]
  lazy val video: lila.video.Env = wire[lila.video.Env]
  lazy val insight: lila.insight.Env = wire[lila.insight.Env]
  lazy val push: lila.push.Env = wire[lila.push.Env]
  lazy val challenge: lila.challenge.Env = wire[lila.challenge.Env]
  lazy val explorer: lila.explorer.Env = wire[lila.explorer.Env]
  lazy val fide: lila.fide.Env = wire[lila.fide.Env]
  lazy val study: lila.study.Env = wire[lila.study.Env]
  lazy val studySearch: lila.studySearch.Env = wire[lila.studySearch.Env]
  lazy val learn: lila.learn.Env = wire[lila.learn.Env]
  lazy val plan: lila.plan.Env = wire[lila.plan.Env]
  lazy val event: lila.event.Env = wire[lila.event.Env]
  lazy val coach: lila.coach.Env = wire[lila.coach.Env]
  lazy val clas: lila.clas.Env = wire[lila.clas.Env]
  lazy val practice: lila.practice.Env = wire[lila.practice.Env]
  lazy val irwin: lila.irwin.Env = wire[lila.irwin.Env]
  lazy val ublog: lila.ublog.Env = wire[lila.ublog.Env]
  lazy val activity: lila.activity.Env = wire[lila.activity.Env]
  lazy val relay: lila.relay.Env = wire[lila.relay.Env]
  lazy val streamer: lila.streamer.Env = wire[lila.streamer.Env]
  lazy val bot: lila.bot.Env = wire[lila.bot.Env]
  lazy val storm: lila.storm.Env = wire[lila.storm.Env]
  lazy val analysisApi: lila.analysisApi.Env = wire[lila.analysisApi.Env]
  lazy val racer: lila.racer.Env = wire[lila.racer.Env]
  lazy val jsBot: lila.jsBot.Env = wire[lila.jsBot.Env]
  lazy val opening: lila.opening.Env = wire[lila.opening.Env]
  lazy val tutor: lila.tutor.Env = wire[lila.tutor.Env]
  lazy val recap: lila.recap.Env = wire[lila.recap.Env]
  lazy val cms: lila.cms.Env = wire[lila.cms.Env]
  lazy val web: lila.web.Env = wire[lila.web.Env]
  lazy val api: lila.api.Env = wire[lila.api.Env]

  lazy val preloader = wire[mashup.Preload]
  lazy val socialInfo = wire[mashup.UserInfo.SocialApi]
  lazy val userNbGames = wire[mashup.UserInfo.NbGamesApi]
  lazy val userInfo = wire[mashup.UserInfo.UserInfoApi]
  lazy val teamInfo = wire[mashup.TeamInfoApi]
  lazy val gamePaginator = wire[mashup.GameFilterMenu.PaginatorBuilder]
  lazy val pageCache = wire[http.PageCache]

  lila.common.Bus.sub[lila.tv.RenderFeaturedJs]:
    case lila.tv.RenderFeaturedJs(game, promise) =>
      promise.success(Html(views.game.mini.noCtx(Pov.naturalOrientation(game), tv = true)))
  lila.common.Bus.sub[lila.puzzle.DailyPuzzle.Render]:
    case lila.puzzle.DailyPuzzle.Render(puzzle, fen, lastMove, promise) =>
      promise.success(Html(views.puzzle.bits.daily(puzzle, fen, lastMove)))

end Env
