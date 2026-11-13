import 'dart:convert';
import 'dart:io';

import 'package:home_widget/home_widget.dart';
import 'package:http/http.dart' as http;
import 'package:path_provider/path_provider.dart';

import '../models/receita.dart';

/// Nome da classe Kotlin do widget (android/app/src/main/kotlin/.../ReceitaWidgetProvider.kt).
const String nomeWidgetAndroid = 'ReceitaWidgetProvider';

/// Chave usada para salvar o JSON das receitas lido pelo widget nativo.
const String chaveReceitasWidget = 'receitas_widget';

/// Envia as receitas para o Home Screen Widget e pede para fixá-lo na tela inicial.
///
/// Retorna `true` se o pedido para fixar o widget foi exibido ao usuário.
Future<bool> criarWidgetReceitas(List<Receita> receitas) async {
  if (!Platform.isAndroid) {
    throw UnsupportedError(
      'O Home Screen Widget só está disponível no Android.',
    );
  }

  if (receitas.isEmpty) {
    throw StateError('Nenhuma receita carregada para mostrar no widget.');
  }

  final pasta = await getApplicationSupportDirectory();
  final pastaImagens = Directory('${pasta.path}/widget_imagens');

  if (!await pastaImagens.exists()) {
    await pastaImagens.create(recursive: true);
  }

  // Baixa todas as imagens ao mesmo tempo para ser mais rápido
  final caminhos = await Future.wait([
    for (var i = 0; i < receitas.length; i++)
      _baixarImagem(receitas[i].img, pastaImagens, i),
  ]);

  final dados = <Map<String, String>>[
    for (var i = 0; i < receitas.length; i++)
      {
        'nome': receitas[i].nome,
        'tipo': receitas[i].tipo,
        'imagem': caminhos[i] ?? '',
      },
  ];

  await HomeWidget.saveWidgetData<String>(
    chaveReceitasWidget,
    jsonEncode(dados),
  );

  await HomeWidget.updateWidget(androidName: nomeWidgetAndroid);

  final suportaFixar = await HomeWidget.isRequestPinWidgetSupported() ?? false;

  if (suportaFixar) {
    await HomeWidget.requestPinWidget(androidName: nomeWidgetAndroid);
  }

  return suportaFixar;
}

/// Baixa a imagem da receita e salva em um arquivo local.
/// Retorna o caminho do arquivo ou `null` se der algum erro.
Future<String?> _baixarImagem(String url, Directory pasta, int indice) async {
  if (!url.startsWith('http')) return null;

  try {
    final resposta = await http
        .get(Uri.parse(url))
        .timeout(const Duration(seconds: 8));

    if (resposta.statusCode != 200 || resposta.bodyBytes.isEmpty) {
      return null;
    }

    final arquivo = File('${pasta.path}/receita_$indice.img');
    await arquivo.writeAsBytes(resposta.bodyBytes, flush: true);

    return arquivo.path;
  } catch (_) {
    return null;
  }
}
