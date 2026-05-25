package org.arcanaforge.app.core.database

internal data class TarotMeaningSeed(
    val keywords: List<String>,
    val uprightMeaning: String,
    val reversedMeaning: String,
)

internal object StandardTarotMeanings {
    private val meanings = mapOf(
        "standard-tarot-major-00" to TarotMeaningSeed(
            keywords = listOf("beginning", "trust", "openness", "risk"),
            uprightMeaning = "A new path is opening. Step forward with curiosity, but keep enough awareness to notice what the moment asks of you.",
            reversedMeaning = "Impulsiveness, avoidance, or fear of the unknown may be shaping the choice. Slow down and check what support or information is missing.",
        ),
        "standard-tarot-major-01" to TarotMeaningSeed(
            keywords = listOf("will", "skill", "focus", "manifestation"),
            uprightMeaning = "You have tools, attention, and agency available. Gather your resources and act with clear intention.",
            reversedMeaning = "Scattered focus or misused influence can weaken the work. Reconnect action to integrity before pushing ahead.",
        ),
        "standard-tarot-major-02" to TarotMeaningSeed(
            keywords = listOf("intuition", "mystery", "inner knowing", "silence"),
            uprightMeaning = "Not everything needs to be forced into daylight yet. Listen beneath the obvious and let intuition inform the next step.",
            reversedMeaning = "Inner signals may be blocked by noise, secrecy, or self-doubt. Create quiet and ask what you already know.",
        ),
        "standard-tarot-major-03" to TarotMeaningSeed(
            keywords = listOf("growth", "care", "abundance", "creation"),
            uprightMeaning = "Nurture what is alive and ready to grow. Care, beauty, patience, and embodiment can make the path more fertile.",
            reversedMeaning = "Overgiving, neglect, or creative stagnation may be present. Restore nourishment before demanding more output.",
        ),
        "standard-tarot-major-04" to TarotMeaningSeed(
            keywords = listOf("structure", "authority", "stability", "boundaries"),
            uprightMeaning = "Clear structure can protect what matters. Lead with steadiness, define boundaries, and make the practical plan visible.",
            reversedMeaning = "Control, rigidity, or weak boundaries may be distorting the situation. Rebuild authority without becoming harsh.",
        ),
        "standard-tarot-major-05" to TarotMeaningSeed(
            keywords = listOf("tradition", "teaching", "values", "guidance"),
            uprightMeaning = "Seek wisdom from a tradition, mentor, practice, or shared set of values. The lesson is strengthened by commitment.",
            reversedMeaning = "Inherited rules may not fit the truth of this moment. Question the structure and choose values consciously.",
        ),
        "standard-tarot-major-06" to TarotMeaningSeed(
            keywords = listOf("choice", "union", "values", "relationship"),
            uprightMeaning = "A meaningful choice asks for alignment between desire, values, and action. Connection deepens when it is chosen honestly.",
            reversedMeaning = "Misalignment, avoidance, or divided loyalty may be creating strain. Clarify what you are choosing and why.",
        ),
        "standard-tarot-major-07" to TarotMeaningSeed(
            keywords = listOf("direction", "discipline", "momentum", "willpower"),
            uprightMeaning = "Focused effort can move the situation forward. Hold the reins, name the destination, and keep conflicting forces in balance.",
            reversedMeaning = "Momentum may be scattered or overcontrolled. Pause long enough to realign drive with direction.",
        ),
        "standard-tarot-major-08" to TarotMeaningSeed(
            keywords = listOf("courage", "patience", "compassion", "self-mastery"),
            uprightMeaning = "Gentle strength is more useful than force. Meet intensity with courage, patience, and steady self-trust.",
            reversedMeaning = "Self-doubt, frustration, or reactive force may be draining strength. Return to calm discipline and kindness toward yourself.",
        ),
        "standard-tarot-major-09" to TarotMeaningSeed(
            keywords = listOf("solitude", "reflection", "wisdom", "searching"),
            uprightMeaning = "Step back from noise and follow the inner lamp. Reflection can reveal the guidance that activity has obscured.",
            reversedMeaning = "Isolation or withdrawal may have gone too far. Bring what you have learned back into contact with life.",
        ),
        "standard-tarot-major-10" to TarotMeaningSeed(
            keywords = listOf("cycles", "change", "turning point", "timing"),
            uprightMeaning = "A cycle is turning. Work with change rather than clinging to the exact shape of the past.",
            reversedMeaning = "Resistance to change or a repeated pattern may be keeping the wheel stuck. Notice the cycle before reacting to it.",
        ),
        "standard-tarot-major-11" to TarotMeaningSeed(
            keywords = listOf("fairness", "truth", "accountability", "balance"),
            uprightMeaning = "Look clearly at cause and effect. Fairness, honesty, and accountability will bring the strongest outcome.",
            reversedMeaning = "Something may be out of balance or unacknowledged. Avoid self-justification and return to the facts.",
        ),
        "standard-tarot-major-12" to TarotMeaningSeed(
            keywords = listOf("pause", "surrender", "perspective", "release"),
            uprightMeaning = "A willing pause can change the view. Release the need to force movement and let a different perspective emerge.",
            reversedMeaning = "Stalling, resentment, or sacrifice without meaning may be present. Ask what the pause is teaching, then choose consciously.",
        ),
        "standard-tarot-major-13" to TarotMeaningSeed(
            keywords = listOf("ending", "transition", "release", "renewal"),
            uprightMeaning = "Something is ready to end so life can reorganize. Letting go creates room for renewal.",
            reversedMeaning = "Resistance to an ending may be prolonging discomfort. Grieve what is changing and loosen your grip.",
        ),
        "standard-tarot-major-14" to TarotMeaningSeed(
            keywords = listOf("integration", "moderation", "healing", "balance"),
            uprightMeaning = "Blend extremes into something sustainable. Patience, proportion, and integration are the path forward.",
            reversedMeaning = "Imbalance or impatience may be disrupting the process. Restore rhythm before trying to solve everything at once.",
        ),
        "standard-tarot-major-15" to TarotMeaningSeed(
            keywords = listOf("attachment", "shadow", "temptation", "constraint"),
            uprightMeaning = "Notice where attachment, fear, or habit has more power than it deserves. Awareness begins the process of freedom.",
            reversedMeaning = "A bond or pattern can loosen now, but denial may still keep it alive. Choose one concrete act of liberation.",
        ),
        "standard-tarot-major-16" to TarotMeaningSeed(
            keywords = listOf("disruption", "truth", "breakdown", "awakening"),
            uprightMeaning = "A false structure may be breaking open. The disruption is uncomfortable, but it can reveal what is real.",
            reversedMeaning = "A needed change may be delayed or internalized. Attend to the warning signs before pressure builds further.",
        ),
        "standard-tarot-major-17" to TarotMeaningSeed(
            keywords = listOf("hope", "renewal", "guidance", "openness"),
            uprightMeaning = "Hope returns through honesty and openness. Let renewal be simple, quiet, and trustworthy.",
            reversedMeaning = "Discouragement or guardedness may dim the view. Begin with one small act that restores faith in the path.",
        ),
        "standard-tarot-major-18" to TarotMeaningSeed(
            keywords = listOf("dreams", "uncertainty", "imagination", "subconscious"),
            uprightMeaning = "The path is lit by symbols rather than certainty. Move carefully through ambiguity and honor what the deeper self reveals.",
            reversedMeaning = "Confusion may begin to clear, or fear may be distorting perception. Test impressions gently against reality.",
        ),
        "standard-tarot-major-19" to TarotMeaningSeed(
            keywords = listOf("joy", "clarity", "vitality", "success"),
            uprightMeaning = "Clarity and warmth are available. Let yourself be seen, celebrate progress, and choose what supports aliveness.",
            reversedMeaning = "Joy may feel delayed, muted, or hard to receive. Look for where comparison or doubt blocks the light.",
        ),
        "standard-tarot-major-20" to TarotMeaningSeed(
            keywords = listOf("calling", "reckoning", "awakening", "renewal"),
            uprightMeaning = "A larger call asks for honest review and response. Let the past teach you without trapping you there.",
            reversedMeaning = "Self-judgment or avoidance may be blocking renewal. Answer the call in a practical, grounded way.",
        ),
        "standard-tarot-major-21" to TarotMeaningSeed(
            keywords = listOf("completion", "integration", "wholeness", "arrival"),
            uprightMeaning = "A cycle reaches completion. Gather the lessons, honor the integration, and prepare for a wider horizon.",
            reversedMeaning = "Closure may be incomplete or hard to claim. Name the unfinished piece and give the cycle a conscious ending.",
        ),
        "standard-tarot-wands-ace" to TarotMeaningSeed(
            keywords = listOf("spark", "inspiration", "desire", "potential"),
            uprightMeaning = "A fresh spark of energy wants expression. Follow inspiration while it is alive and give it a first concrete form.",
            reversedMeaning = "The spark may be blocked, rushed, or unfocused. Protect the idea long enough to understand what it needs.",
        ),
        "standard-tarot-wands-two" to TarotMeaningSeed(
            keywords = listOf("planning", "vision", "choice", "expansion"),
            uprightMeaning = "A wider possibility is visible. Plan from a larger perspective and choose the direction that matches your long-term vision.",
            reversedMeaning = "Hesitation or narrow planning may limit growth. Revisit the choice before settling for what is familiar.",
        ),
        "standard-tarot-wands-three" to TarotMeaningSeed(
            keywords = listOf("expansion", "foresight", "progress", "waiting"),
            uprightMeaning = "Early effort is beginning to reach outward. Stay prepared, watch for returning signals, and keep building from the horizon.",
            reversedMeaning = "Plans may be delayed or too small for the vision. Adjust expectations without abandoning forward movement.",
        ),
        "standard-tarot-wands-four" to TarotMeaningSeed(
            keywords = listOf("celebration", "home", "milestone", "welcome"),
            uprightMeaning = "A milestone deserves recognition. Stability, belonging, and shared joy can strengthen the foundation.",
            reversedMeaning = "Celebration may feel delayed, private, or unstable. Ask what would make belonging feel more genuine.",
        ),
        "standard-tarot-wands-five" to TarotMeaningSeed(
            keywords = listOf("conflict", "competition", "friction", "practice"),
            uprightMeaning = "Friction is active, but it can sharpen skill and clarify priorities. Stay engaged without turning every difference into a battle.",
            reversedMeaning = "Conflict may be avoided, internalized, or ready to settle. Choose whether the struggle is useful or merely draining.",
        ),
        "standard-tarot-wands-six" to TarotMeaningSeed(
            keywords = listOf("recognition", "victory", "confidence", "support"),
            uprightMeaning = "Progress is visible and support can be received. Let recognition strengthen confidence without making it the whole goal.",
            reversedMeaning = "Validation may be delayed or feel hollow. Reconnect success to inner purpose instead of applause alone.",
        ),
        "standard-tarot-wands-seven" to TarotMeaningSeed(
            keywords = listOf("defense", "conviction", "pressure", "standing firm"),
            uprightMeaning = "Stand for what matters even under pressure. Clear conviction helps you choose which challenges deserve your energy.",
            reversedMeaning = "Defensiveness or exhaustion may be weakening your position. Protect your ground without fighting every front.",
        ),
        "standard-tarot-wands-eight" to TarotMeaningSeed(
            keywords = listOf("speed", "movement", "message", "momentum"),
            uprightMeaning = "Energy is moving quickly. Respond while momentum is available, and keep communication clear.",
            reversedMeaning = "Delays, mixed signals, or haste may scatter the energy. Slow the pace enough to aim well.",
        ),
        "standard-tarot-wands-nine" to TarotMeaningSeed(
            keywords = listOf("resilience", "guardedness", "persistence", "boundaries"),
            uprightMeaning = "You have endured more than one challenge. Stay resilient, but let boundaries protect rather than isolate you.",
            reversedMeaning = "Weariness or defensiveness may be taking over. Rest, reassess, and decide what still truly needs guarding.",
        ),
        "standard-tarot-wands-ten" to TarotMeaningSeed(
            keywords = listOf("burden", "responsibility", "effort", "overload"),
            uprightMeaning = "A heavy load may be close to completion, but it is still heavy. Delegate, simplify, or name what you no longer need to carry.",
            reversedMeaning = "The burden may be unsustainable or ready to be released. Let responsibility become more honest and shared.",
        ),
        "standard-tarot-wands-page" to TarotMeaningSeed(
            keywords = listOf("enthusiasm", "discovery", "message", "creative spark"),
            uprightMeaning = "Curiosity and creative courage are waking up. Explore the spark without demanding mastery too soon.",
            reversedMeaning = "Restlessness or self-doubt may interrupt the beginning. Give the idea a small experiment instead of judging it early.",
        ),
        "standard-tarot-wands-knight" to TarotMeaningSeed(
            keywords = listOf("adventure", "drive", "impulse", "boldness"),
            uprightMeaning = "Bold energy wants movement. Act with courage, but keep enough awareness to avoid burning past the details.",
            reversedMeaning = "Impulsiveness, impatience, or scattered passion may be present. Direct the fire before it consumes the plan.",
        ),
        "standard-tarot-wands-queen" to TarotMeaningSeed(
            keywords = listOf("confidence", "warmth", "creativity", "magnetism"),
            uprightMeaning = "Lead from warmth, confidence, and creative self-possession. Your presence can encourage growth around you.",
            reversedMeaning = "Confidence may turn inward, flicker, or seek control. Reclaim your fire without comparing it to anyone else's.",
        ),
        "standard-tarot-wands-king" to TarotMeaningSeed(
            keywords = listOf("leadership", "vision", "enterprise", "maturity"),
            uprightMeaning = "Vision needs mature leadership. Set direction, inspire action, and take responsibility for the fire you carry.",
            reversedMeaning = "Forceful leadership or unfocused ambition may distort the vision. Lead with clarity rather than dominance.",
        ),
        "standard-tarot-cups-ace" to TarotMeaningSeed(
            keywords = listOf("feeling", "opening", "compassion", "new love"),
            uprightMeaning = "The heart is opening to a fresh flow of feeling. Receive compassion, connection, and creative emotional renewal.",
            reversedMeaning = "Feelings may be blocked, withheld, or overflowing privately. Tend the heart before asking it to pour outward.",
        ),
        "standard-tarot-cups-two" to TarotMeaningSeed(
            keywords = listOf("bond", "mutuality", "partnership", "attraction"),
            uprightMeaning = "Mutual recognition can deepen connection. Meet the other with honesty, respect, and willingness to share.",
            reversedMeaning = "Imbalance or disconnection may need attention. Restore honest exchange before expecting harmony.",
        ),
        "standard-tarot-cups-three" to TarotMeaningSeed(
            keywords = listOf("friendship", "celebration", "community", "support"),
            uprightMeaning = "Shared joy and support matter. Let community, friendship, or creative collaboration refill the cup.",
            reversedMeaning = "Social strain, overextension, or feeling outside the circle may be present. Choose connection that feels nourishing.",
        ),
        "standard-tarot-cups-four" to TarotMeaningSeed(
            keywords = listOf("apathy", "reflection", "discontent", "reconsideration"),
            uprightMeaning = "A pause in desire may reveal what no longer satisfies. Look again before refusing what is quietly offered.",
            reversedMeaning = "Interest may be returning after withdrawal. Reopen gently, but do not accept what still feels empty.",
        ),
        "standard-tarot-cups-five" to TarotMeaningSeed(
            keywords = listOf("grief", "regret", "loss", "perspective"),
            uprightMeaning = "Grief or disappointment deserves honesty. Let loss be felt while remembering that not every source of support is gone.",
            reversedMeaning = "Healing begins when regret no longer holds the whole story. Turn slowly toward what remains possible.",
        ),
        "standard-tarot-cups-six" to TarotMeaningSeed(
            keywords = listOf("memory", "kindness", "nostalgia", "innocence"),
            uprightMeaning = "Memory, tenderness, or a simpler kind of care may be relevant. Receive the gift without living only in the past.",
            reversedMeaning = "Nostalgia may be clouding the present, or old patterns may be ready to mature. Honor the past without returning unchanged.",
        ),
        "standard-tarot-cups-seven" to TarotMeaningSeed(
            keywords = listOf("options", "fantasy", "imagination", "discernment"),
            uprightMeaning = "Many possibilities compete for attention. Imagination is useful, but discernment must choose what is real enough to pursue.",
            reversedMeaning = "Confusion can clear when you narrow the field. Choose substance over illusion or wishful thinking.",
        ),
        "standard-tarot-cups-eight" to TarotMeaningSeed(
            keywords = listOf("departure", "searching", "release", "deeper meaning"),
            uprightMeaning = "Something once meaningful may no longer be enough. Walking away can be an act of loyalty to deeper truth.",
            reversedMeaning = "Leaving may be delayed, feared, or reconsidered. Ask whether staying protects growth or postpones it.",
        ),
        "standard-tarot-cups-nine" to TarotMeaningSeed(
            keywords = listOf("contentment", "satisfaction", "wish", "pleasure"),
            uprightMeaning = "Satisfaction is available and worth receiving. Enjoy what has been gathered without losing generosity or perspective.",
            reversedMeaning = "Pleasure may feel shallow, delayed, or disconnected from true need. Ask what would make fulfillment more honest.",
        ),
        "standard-tarot-cups-ten" to TarotMeaningSeed(
            keywords = listOf("harmony", "family", "belonging", "emotional fulfillment"),
            uprightMeaning = "Emotional harmony is possible through shared care and belonging. Notice the relationships that help life feel whole.",
            reversedMeaning = "The picture of happiness may not match reality. Repair connection by naming what is needed beneath the ideal.",
        ),
        "standard-tarot-cups-page" to TarotMeaningSeed(
            keywords = listOf("sensitivity", "message", "imagination", "openness"),
            uprightMeaning = "A tender message, intuition, or creative feeling is emerging. Stay receptive to the small voice of the heart.",
            reversedMeaning = "Emotional immaturity or guarded sensitivity may be present. Give feelings language without letting them rule everything.",
        ),
        "standard-tarot-cups-knight" to TarotMeaningSeed(
            keywords = listOf("romance", "invitation", "idealism", "devotion"),
            uprightMeaning = "Move with sincerity, imagination, and emotional grace. An invitation or heartfelt pursuit may be meaningful.",
            reversedMeaning = "Idealism may drift into avoidance, moodiness, or mixed signals. Ground the feeling in honest action.",
        ),
        "standard-tarot-cups-queen" to TarotMeaningSeed(
            keywords = listOf("empathy", "intuition", "care", "emotional wisdom"),
            uprightMeaning = "Deep feeling and intuition can guide wisely. Offer care without losing the boundary between your emotions and another's.",
            reversedMeaning = "Emotional overwhelm or self-neglect may blur judgment. Return compassion to yourself as well as others.",
        ),
        "standard-tarot-cups-king" to TarotMeaningSeed(
            keywords = listOf("emotional balance", "diplomacy", "compassion", "maturity"),
            uprightMeaning = "Steady emotional leadership is called for. Feel deeply, respond wisely, and hold space without being swept away.",
            reversedMeaning = "Emotions may be controlled, suppressed, or used indirectly. Choose maturity over avoidance or manipulation.",
        ),
        "standard-tarot-swords-ace" to TarotMeaningSeed(
            keywords = listOf("clarity", "truth", "decision", "breakthrough"),
            uprightMeaning = "A clear truth or decisive idea cuts through confusion. Use the insight with precision and integrity.",
            reversedMeaning = "Confusion, harsh words, or unclear thinking may distort the issue. Wait for truth before making the cut.",
        ),
        "standard-tarot-swords-two" to TarotMeaningSeed(
            keywords = listOf("stalemate", "choice", "avoidance", "balance"),
            uprightMeaning = "A decision is being held at a tense pause. Remove the blindfold gently and let more truth into the choice.",
            reversedMeaning = "Avoidance may be breaking down, or indecision may be intensifying. Choose the next honest fact to face.",
        ),
        "standard-tarot-swords-three" to TarotMeaningSeed(
            keywords = listOf("heartbreak", "truth", "sorrow", "release"),
            uprightMeaning = "Painful truth may need to be acknowledged directly. Naming the wound begins the process of release.",
            reversedMeaning = "Healing is possible when old hurt is not continually reopened. Let grief move without making it permanent identity.",
        ),
        "standard-tarot-swords-four" to TarotMeaningSeed(
            keywords = listOf("rest", "recovery", "stillness", "retreat"),
            uprightMeaning = "Rest is part of the work. Step back, recover strength, and let the mind settle before reengaging.",
            reversedMeaning = "Restlessness or burnout may show that recovery has been postponed. Return to restoration before collapse demands it.",
        ),
        "standard-tarot-swords-five" to TarotMeaningSeed(
            keywords = listOf("conflict", "cost", "ego", "aftermath"),
            uprightMeaning = "A win may carry a cost. Consider whether being right, dominant, or defended is worth the damage created.",
            reversedMeaning = "Repair or disengagement may be possible after conflict. Lay down the need to keep proving the point.",
        ),
        "standard-tarot-swords-six" to TarotMeaningSeed(
            keywords = listOf("transition", "passage", "recovery", "moving on"),
            uprightMeaning = "A difficult passage is moving toward calmer waters. Accept help and let transition happen one crossing at a time.",
            reversedMeaning = "Moving on may feel delayed or emotionally unfinished. Identify what still needs safe passage.",
        ),
        "standard-tarot-swords-seven" to TarotMeaningSeed(
            keywords = listOf("strategy", "secrecy", "self-protection", "caution"),
            uprightMeaning = "Strategy and discretion may be needed, but motives matter. Choose careful action without betraying your integrity.",
            reversedMeaning = "Hidden information may surface, or a strategy may need revision. Be honest about what is being avoided.",
        ),
        "standard-tarot-swords-eight" to TarotMeaningSeed(
            keywords = listOf("restriction", "fear", "limitation", "beliefs"),
            uprightMeaning = "The situation may feel more trapped than it is. Examine the beliefs, fears, or assumptions tightening the bindings.",
            reversedMeaning = "A limiting thought pattern can loosen. Take one practical step that proves movement is possible.",
        ),
        "standard-tarot-swords-nine" to TarotMeaningSeed(
            keywords = listOf("anxiety", "worry", "guilt", "night thoughts"),
            uprightMeaning = "Anxiety may be magnifying the pain in private. Bring the fear into language, support, and proportion.",
            reversedMeaning = "Worry may be easing or asking for real help. Do not suffer alone with thoughts that need care.",
        ),
        "standard-tarot-swords-ten" to TarotMeaningSeed(
            keywords = listOf("ending", "collapse", "release", "finality"),
            uprightMeaning = "A painful ending has reached its limit. Stop arguing with what is done and look for the first sign of dawn.",
            reversedMeaning = "Recovery begins after collapse, but clinging to the old pain can delay it. Let the ending become complete.",
        ),
        "standard-tarot-swords-page" to TarotMeaningSeed(
            keywords = listOf("curiosity", "study", "alertness", "message"),
            uprightMeaning = "A quick mind wants to investigate. Ask questions, gather facts, and keep communication sharp but fair.",
            reversedMeaning = "Restless thoughts, gossip, or defensiveness may scatter clarity. Slow the mind and verify before speaking.",
        ),
        "standard-tarot-swords-knight" to TarotMeaningSeed(
            keywords = listOf("speed", "argument", "ambition", "directness"),
            uprightMeaning = "Direct action and clear words can cut through delay. Aim carefully so speed does not become recklessness.",
            reversedMeaning = "Rushing, arguing, or charging ahead may create avoidable damage. Pause and refine the target.",
        ),
        "standard-tarot-swords-queen" to TarotMeaningSeed(
            keywords = listOf("discernment", "honesty", "boundaries", "independence"),
            uprightMeaning = "Clear perception and honest boundaries are strengths. Speak truth with enough compassion to keep it useful.",
            reversedMeaning = "Sharpness may become coldness, or truth may be withheld. Reclaim clarity without using it as armor.",
        ),
        "standard-tarot-swords-king" to TarotMeaningSeed(
            keywords = listOf("judgment", "logic", "authority", "strategy"),
            uprightMeaning = "Reasoned authority is needed. Make decisions from principles, evidence, and a clear ethical frame.",
            reversedMeaning = "Logic may be used rigidly or unfairly. Check whether authority is serving truth or merely control.",
        ),
        "standard-tarot-pentacles-ace" to TarotMeaningSeed(
            keywords = listOf("opportunity", "resource", "body", "seed"),
            uprightMeaning = "A grounded opportunity is available. Plant the seed with practical care and let value grow over time.",
            reversedMeaning = "A chance may be missed, delayed, or undernourished. Clarify what resources are needed before it can take root.",
        ),
        "standard-tarot-pentacles-two" to TarotMeaningSeed(
            keywords = listOf("balance", "adaptation", "priorities", "change"),
            uprightMeaning = "Multiple demands need flexible balance. Adjust rhythm, prioritize clearly, and keep the moving pieces honest.",
            reversedMeaning = "Overcommitment or unstable priorities may be showing. Simplify before the juggling becomes unsustainable.",
        ),
        "standard-tarot-pentacles-three" to TarotMeaningSeed(
            keywords = listOf("craft", "teamwork", "learning", "collaboration"),
            uprightMeaning = "Skill grows through collaboration, feedback, and steady craft. Let shared standards improve the work.",
            reversedMeaning = "Misalignment, poor planning, or undervalued skill may weaken the project. Clarify roles and expectations.",
        ),
        "standard-tarot-pentacles-four" to TarotMeaningSeed(
            keywords = listOf("security", "control", "holding on", "stability"),
            uprightMeaning = "Security matters, but holding too tightly can limit life. Protect what is valuable without closing the hand completely.",
            reversedMeaning = "Scarcity, rigidity, or financial tension may be loosening. Practice wiser stewardship instead of fear-based control.",
        ),
        "standard-tarot-pentacles-five" to TarotMeaningSeed(
            keywords = listOf("hardship", "exclusion", "need", "support"),
            uprightMeaning = "Difficulty or lack may feel isolating, but support may be closer than it appears. Let need be named clearly.",
            reversedMeaning = "Recovery can begin when help is received or practical options are seen. Move toward warmth, not shame.",
        ),
        "standard-tarot-pentacles-six" to TarotMeaningSeed(
            keywords = listOf("generosity", "exchange", "support", "fairness"),
            uprightMeaning = "Giving and receiving need balance. Notice power, fairness, and the dignity of everyone in the exchange.",
            reversedMeaning = "Unequal support, strings attached, or difficulty receiving may be present. Restore fairness where resources move.",
        ),
        "standard-tarot-pentacles-seven" to TarotMeaningSeed(
            keywords = listOf("patience", "investment", "assessment", "growth"),
            uprightMeaning = "Long-term effort is still growing. Pause to assess what is working before deciding whether to continue, prune, or redirect.",
            reversedMeaning = "Impatience or poor return may be surfacing. Reevaluate where your energy is invested.",
        ),
        "standard-tarot-pentacles-eight" to TarotMeaningSeed(
            keywords = listOf("practice", "skill", "diligence", "apprenticeship"),
            uprightMeaning = "Consistent practice matters. Focus on the craft, refine the details, and let mastery build through repetition.",
            reversedMeaning = "Perfectionism, boredom, or uneven effort may block progress. Return to purposeful practice, not busywork.",
        ),
        "standard-tarot-pentacles-nine" to TarotMeaningSeed(
            keywords = listOf("self-sufficiency", "pleasure", "refinement", "harvest"),
            uprightMeaning = "Enjoy the fruits of discipline and self-respect. Independence can feel graceful when it includes gratitude.",
            reversedMeaning = "Worth, comfort, or independence may feel unstable. Reconnect pleasure to self-trust rather than performance.",
        ),
        "standard-tarot-pentacles-ten" to TarotMeaningSeed(
            keywords = listOf("legacy", "family", "stability", "inheritance"),
            uprightMeaning = "Long-term stability, lineage, or shared resources are in focus. Consider what is being built to last.",
            reversedMeaning = "Family patterns, unstable foundations, or inherited expectations may need review. Choose what legacy is worth continuing.",
        ),
        "standard-tarot-pentacles-page" to TarotMeaningSeed(
            keywords = listOf("study", "manifestation", "practice", "new skill"),
            uprightMeaning = "A practical beginning asks for study and commitment. Treat the opportunity as something you can learn into.",
            reversedMeaning = "Lack of focus or follow-through may slow the beginning. Make the goal tangible and start small.",
        ),
        "standard-tarot-pentacles-knight" to TarotMeaningSeed(
            keywords = listOf("reliability", "routine", "patience", "work"),
            uprightMeaning = "Slow, reliable effort will carry the work forward. Honor routines, details, and promises.",
            reversedMeaning = "Stagnation, stubbornness, or overwork may be present. Adjust the routine so steadiness does not become stuckness.",
        ),
        "standard-tarot-pentacles-queen" to TarotMeaningSeed(
            keywords = listOf("nurturing", "practical care", "comfort", "resourcefulness"),
            uprightMeaning = "Practical care creates real safety. Tend the body, home, resources, and people with grounded generosity.",
            reversedMeaning = "Care may become overextension, worry, or neglect of your own needs. Bring nourishment back into balance.",
        ),
        "standard-tarot-pentacles-king" to TarotMeaningSeed(
            keywords = listOf("stewardship", "success", "security", "leadership"),
            uprightMeaning = "Material wisdom and steady stewardship are available. Build security that is generous, ethical, and durable.",
            reversedMeaning = "Control, greed, or fear of loss may distort stewardship. Measure success by integrity as well as results.",
        ),
    )

    fun forCard(cardId: String): TarotMeaningSeed? = meanings[cardId]
}
