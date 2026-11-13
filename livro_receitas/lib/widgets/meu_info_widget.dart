import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

class MeuInfoWidget extends StatelessWidget {
  final String informacao;
  final IconData icone;

  const MeuInfoWidget({
    super.key,
    required this.informacao,
    this.icone = Icons.info_outline,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: AppTheme.superficie,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppTheme.laranja, width: 1.2),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: const BoxDecoration(
              color: AppTheme.laranjaTransparente,
              shape: BoxShape.circle,
            ),
            child: Icon(icone, color: AppTheme.laranja, size: 22),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              informacao,
              style: const TextStyle(
                color: AppTheme.texto,
                fontWeight: FontWeight.bold,
                fontSize: 15,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
