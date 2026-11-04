package fr.formation.recettes.ui.ajout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.formation.recettes.R
import fr.formation.recettes.logique.DIFFICULTE_MAX
import fr.formation.recettes.logique.DIFFICULTE_MIN
import fr.formation.recettes.logique.DUREE_MAX_MINUTES
import fr.formation.recettes.logique.ErreurDuree
import fr.formation.recettes.logique.ErreurNom
import fr.formation.recettes.logique.FormulaireRecette
import fr.formation.recettes.logique.FormulaireSaver
import fr.formation.recettes.logique.NOM_LONGUEUR_MAX
import fr.formation.recettes.modele.Categorie
import fr.formation.recettes.ui.theme.CarnetRecettesTheme
import kotlin.math.roundToInt

/** Version AVEC état du formulaire : la saisie survit à la rotation (`rememberSaveable` + `FormulaireSaver`). */
@Composable
fun AjoutRecetteRoute(
    onEnregistrer: (FormulaireRecette) -> Unit,
    modifier: Modifier = Modifier,
) {
    var formulaire by rememberSaveable(stateSaver = FormulaireSaver) { mutableStateOf(FormulaireRecette()) }
    AjoutRecetteScreen(
        formulaire = formulaire,
        onFormulaireChange = { formulaire = it },
        onEnregistrer = { onEnregistrer(formulaire) },
        modifier = modifier,
    )
}

/**
 * Formulaire d'ajout, SANS état. Les règles de validation sont dans `FormulaireRecette`
 * (testées en unitaire) ; l'écran se contente de les afficher.
 */
@Composable
fun AjoutRecetteScreen(
    formulaire: FormulaireRecette,
    onFormulaireChange: (FormulaireRecette) -> Unit,
    onEnregistrer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)       // largeur limitée sur tablette
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChampNom(formulaire.nom, formulaire.erreurNom, { onFormulaireChange(formulaire.copy(nom = it)) })
            ChampDuree(formulaire.duree, formulaire.erreurDuree, { onFormulaireChange(formulaire.copy(duree = it)) })
            ChoixCategorie(formulaire.categorie, { onFormulaireChange(formulaire.copy(categorie = it)) })
            ChoixDifficulte(formulaire.difficulte, { onFormulaireChange(formulaire.copy(difficulte = it)) })
            OutlinedTextField(
                value = formulaire.ingredients,
                onValueChange = { onFormulaireChange(formulaire.copy(ingredients = it)) },
                label = { Text(stringResource(R.string.ingredients_saisie)) },
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = formulaire.etapes,
                onValueChange = { onFormulaireChange(formulaire.copy(etapes = it)) },
                label = { Text(stringResource(R.string.etapes_saisie)) },
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onEnregistrer,
                enabled = formulaire.estValide,       // désactivé tant que le formulaire est invalide
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag(TAG_BOUTON_ENREGISTRER),
            ) {
                Text(stringResource(R.string.enregistrer))
            }
        }
    }
}

const val TAG_BOUTON_ENREGISTRER = "bouton_enregistrer"
const val TAG_CHAMP_NOM = "champ_nom"
const val TAG_CHAMP_DUREE = "champ_duree"

@Composable
private fun ChampNom(valeur: String, erreur: ErreurNom?, onValeurChange: (String) -> Unit) {
    // Le message « obligatoire » n'est pas affiché en rouge sur un formulaire vierge : moins intrusif
    val afficherErreur = erreur != null && valeur.isNotEmpty()
    OutlinedTextField(
        value = valeur,
        onValueChange = onValeurChange,
        label = { Text(stringResource(R.string.nom_recette)) },
        isError = afficherErreur,
        supportingText = {
            Text(
                when {
                    !afficherErreur -> stringResource(R.string.champ_obligatoire)
                    erreur == ErreurNom.TropLong -> stringResource(R.string.erreur_nom_trop_long, NOM_LONGUEUR_MAX)
                    else -> stringResource(R.string.erreur_nom_obligatoire)
                },
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TAG_CHAMP_NOM),
    )
}

@Composable
private fun ChampDuree(valeur: String, erreur: ErreurDuree?, onValeurChange: (String) -> Unit) {
    val afficherErreur = erreur != null && valeur.isNotEmpty()
    OutlinedTextField(
        value = valeur,
        onValueChange = onValeurChange,
        label = { Text(stringResource(R.string.duree)) },
        suffix = { Text(stringResource(R.string.unite_minutes)) },
        isError = afficherErreur,
        supportingText = {
            Text(
                when (if (afficherErreur) erreur else null) {
                    ErreurDuree.NonNumerique -> stringResource(R.string.erreur_duree_non_numerique)
                    ErreurDuree.HorsLimites -> stringResource(R.string.erreur_duree_hors_limites, DUREE_MAX_MINUTES)
                    ErreurDuree.Obligatoire, null -> stringResource(R.string.aide_duree, DUREE_MAX_MINUTES)
                },
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TAG_CHAMP_DUREE),
    )
}

/** Choix exclusif : groupe de boutons radio, chaque ligne entière est sélectionnable. */
@Composable
private fun ChoixCategorie(choix: Categorie, onChoixChange: (Categorie) -> Unit) {
    Text(stringResource(R.string.categorie), style = MaterialTheme.typography.titleSmall)
    Row(Modifier.selectableGroup()) {
        Categorie.entries.forEach { categorie ->
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .selectable(
                        selected = categorie == choix,
                        onClick = { onChoixChange(categorie) },
                        role = Role.RadioButton,
                    )
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = categorie == choix, onClick = null)
                Text(stringResource(categorie.libelle), Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun ChoixDifficulte(valeur: Float, onValeurChange: (Float) -> Unit) {
    val niveau = valeur.roundToInt()
    val libelle = stringArrayResource(R.array.niveaux_difficulte).getOrElse(niveau - 1) { "" }
    val texte = stringResource(R.string.difficulte_valeur, niveau, libelle)
    Text(texte, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
    Slider(
        value = valeur,
        onValueChange = onValeurChange,
        valueRange = DIFFICULTE_MIN.toFloat()..DIFFICULTE_MAX.toFloat(),
        steps = DIFFICULTE_MAX - DIFFICULTE_MIN - 1,          // 5 positions entières
        modifier = Modifier.semantics { stateDescription = texte },   // lu « 3/5 (Moyenne) » plutôt qu'un pourcentage
    )
}

@PreviewLightDark
@Composable
private fun AjoutRecetteScreenVidePreview() {
    CarnetRecettesTheme {
        Surface { AjoutRecetteScreen(formulaire = FormulaireRecette(), onFormulaireChange = {}, onEnregistrer = {}) }
    }
}

@PreviewLightDark
@Composable
private fun AjoutRecetteScreenErreursPreview() {
    CarnetRecettesTheme {
        Surface {
            AjoutRecetteScreen(
                formulaire = FormulaireRecette(nom = " ", duree = "2000", difficulte = 4f),
                onFormulaireChange = {},
                onEnregistrer = {},
            )
        }
    }
}
